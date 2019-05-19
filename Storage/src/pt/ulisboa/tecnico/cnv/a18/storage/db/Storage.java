package pt.ulisboa.tecnico.cnv.a18.storage.db;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import pt.ulisboa.tecnico.cnv.a18.storage.Request;

public class Storage {

    static AmazonDynamoDB dynamoDB;
    static DynamoDBMapper mapper;

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
        CreateTableRequest request = mapper.generateCreateTableRequest(RequestEntry.class);
        request.setProvisionedThroughput(new ProvisionedThroughput(10L, 10L)); //TODO
        TableUtils.createTableIfNotExists(dynamoDB, request);
        TableUtils.waitUntilActive(dynamoDB, "TableName");
    }

    public static void main(String[] args) throws Exception {
        init();
        store();
        get();

    }

    public static void store(){
        String hashkey = "105L";
        RequestEntry entry = mapper.load(RequestEntry.class, hashkey);
        //Request request = new Request(hashkey);
        if (entry == null){
            //entry = new RequestEntry(request);

        }
        else{
            System.out.println("nothing to do here...");
        }
        mapper.save(entry);
    }

    public static void get(){
        String hashkey = "105L";
        RequestEntry entry = mapper.load(RequestEntry.class, hashkey);
        System.out.println("got " + entry.request.getKey());
    }

}