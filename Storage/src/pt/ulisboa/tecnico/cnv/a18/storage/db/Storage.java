package pt.ulisboa.tecnico.cnv.a18.storage.db;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import pt.ulisboa.tecnico.cnv.a18.storage.Request;

import java.util.HashMap;

public class Storage extends AbstractStorage{

    static AmazonDynamoDB dynamoDB;
    static DynamoDBMapper mapper;
    HashMap<Long, Request> currentRequests = new HashMap<>();

    public Storage() throws Exception{
        init();
    }

    public void setNewRequest(Long id, Request request){
        currentRequests.put(id, request);
    }

    private static void init() throws Exception {
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
        CreateTableRequest request = mapper.generateCreateTableRequest(MetricsStorage.class);
        request.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L)); //TODO
        TableUtils.createTableIfNotExists(dynamoDB, request);
        TableUtils.waitUntilActive(dynamoDB, "MetricsStorage");
    }


    public void storeNumberOfMethods(Long tid, long mcount){
        Request request = currentRequests.get(tid);
        MetricsStorage entry = mapper.load(MetricsStorage.class, request.getKey());
        if (entry == null){
            entry = new MetricsStorage(request);
        }
        entry.setMethodsNumber(mcount);
        entry.setArea(request.getHeight() * request.getWidth());
        entry.setStrategy(request.getStrategy());
        mapper.save(entry);
    }

    public void storeEstimatedMethodsNumber(Request request, Long estimate){
        MetricsStorage entry = mapper.load(MetricsStorage.class, request.getKey());
        if (entry == null){
            entry = new MetricsStorage(request);
        }
        entry.setEstimatedMethodsNumber(estimate);
        mapper.save(entry);
    }



    public static void get(String key){
        MetricsStorage entry = mapper.load(MetricsStorage.class, key);
        System.out.println("got " + entry.getRequestAttribute().getKey());
    }

}