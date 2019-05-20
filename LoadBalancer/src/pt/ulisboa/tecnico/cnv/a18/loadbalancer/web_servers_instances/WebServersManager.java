package pt.ulisboa.tecnico.cnv.a18.loadbalancer.web_servers_instances;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.Collections;

// Singleton
public class WebServersManager {
    static AmazonEC2 ec2;

    private static final WebServersManager webServersManager = new WebServersManager();
    private static ArrayList<WebServerWrapper> webServers = new ArrayList<>();
    // Actual cost for a web server instance
    private ArrayList<Integer> totalCost = new ArrayList<>();

    private WebServersManager() {
        init();
    }

    public static WebServersManager getInstance() {
        return webServersManager;
    }

    private static void init() {

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-east-1").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    //TODO maybe argument with cost of request and search the adequate server
    public WebServerWrapper getWebServer(int requestCode) {
        //choosing instance with less "work" TODO see if this is to simple, maybe if progress is incorporate
        int minInd = minIndex(totalCost);
        //Add to the curret cost of a server requests, the cost of the new request
        totalCost.set(minInd, totalCost.get(minInd) + requestCode);
        return webServers.get(minInd);
    }

    private static int minIndex (ArrayList<Integer> costs) {
        return costs.indexOf (Collections.min(costs)); }


    private void addWebServer(String instanceId) {
        webServers.add(new WebServerWrapper(instanceId));
    }

    public void createNewWebServer() {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        // TODO get these values from properties file
        runInstancesRequest.withImageId("ami-0c80afef49b12a338")
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName("CNV-L3-KEY")
                .withSecurityGroups("CNV-ssh+http");

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);

        String newInstanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();

        addWebServer(newInstanceId);
    }

    // Check instances that exist in amazon and "register" them in the loadBalancer
    private void addExistingInstances() { //TODO this is incomplete and is not beeing used...is it necessary
        AmazonEC2 ec2 = WebServersManager.ec2;

        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while(!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            String id;
            String state = "";
            String ipAddress;
            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "Found instance with id %s, " +
                                    "AMI %s, " +
                                    "type %s, " +
                                    "state %s " +
                                    "monitoring state %s " +
                                    "and ip address %s\n",
                            id = instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            state = instance.getState().getName(),
                            instance.getMonitoring().getState(),
                            ipAddress = instance.getPublicIpAddress());
                }
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
            if(!state.equals("terminated") && !state.equals("shutting-down") && !state.equals("")) {

            }
        }
    }
}