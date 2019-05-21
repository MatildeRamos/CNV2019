package pt.ulisboa.tecnico.cnv.a18.loadbalancer.storage;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import pt.ulisboa.tecnico.cnv.a18.storage.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LBStorage {
    private static LBStorage storage;
    static AmazonDynamoDB dynamoDB;
    static DynamoDBMapper mapper;
    static int AREA_LIMIT = 50000;

    public static synchronized LBStorage getStorage() {
        if (storage == null) {
            storage = new LBStorage();
        }
        return storage;
    }

   private LBStorage(){
        init();
   }

    private static void init() {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        //try {
        credentialsProvider.getCredentials();
        /*} catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }*/

        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")
                .build();


        mapper = new DynamoDBMapper(dynamoDB);
        CreateTableRequest request = mapper.generateCreateTableRequest(EstimateStorage.class);
        request.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L)); //TODO
        TableUtils.createTableIfNotExists(dynamoDB, request);
        try {
            TableUtils.waitUntilActive(dynamoDB, "EstimateStorage");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long calcExpectedNumberofMethods(Request request){
        EstimateStorage estimate = new EstimateStorage(request);
        EstimateStorage entry = mapper.load(EstimateStorage.class, estimate.getStrategy(), estimate.getArea());
        if (entry != null){
            Double d = new Double(entry.getMethodsPerArea() * estimate.getArea());
            return  d.longValue(); //TODO ver onde isto é definido
        }
        else{
            HashMap<String, AttributeValue> newEstimate = new HashMap<>();
            newEstimate.put(":searchStrategy", new AttributeValue().withS(estimate.getStrategy()));
            newEstimate.put(":areaBottomLimit", new AttributeValue().withN(String.valueOf(estimate.getArea() - AREA_LIMIT)));
            newEstimate.put(":areaTopLimit", new AttributeValue().withN(String.valueOf(estimate.getArea() + AREA_LIMIT)));

            DynamoDBQueryExpression<EstimateStorage> queryExpression = new DynamoDBQueryExpression<EstimateStorage>()
                    .withKeyConditionExpression("strategy = :searchStrategy")
                    .withExpressionAttributeValues(newEstimate)
                    .withFilterExpression("area between :areaBottomLimit and :areaTopLimit");

            List<EstimateStorage> iList =  mapper.query(EstimateStorage.class, queryExpression);
            if (iList.isEmpty() || iList == null){
                if(request.getStrategy().equals("ASTAR")){
                    return estimate.getArea() * 500;
                }
                else{
                    return estimate.getArea() * 1000; //TODO ver dummys
                }
            }
            return estimate.getArea() * calcAverageMethodsPerArea(iList);
        }
    }

    public int calcAverageMethodsPerArea(List<EstimateStorage> list){
        int res = 0;
        for (EstimateStorage e : list){
            res += e.getMethodsPerArea();
        }
        return res/list.size();
    }
}
