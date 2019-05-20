package pt.ulisboa.tecnico.cnv.a18.loadbalancer.web_servers_instances;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedParallelScanList;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.cnv.a18.loadbalancer.storage.EstimateStorage;
import pt.ulisboa.tecnico.cnv.a18.storage.db.MetricsStorage;
import pt.ulisboa.tecnico.cnv.a18.storage.db.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Singleton
public class WebServersManager {
    static AmazonEC2 ec2;

    int requestCounter = 0;
    private int NUMBER_OF_REQUESTS_BETWEEN_UPDATES = 5;



    private static ArrayList<WebServerWrapper> webServers = new ArrayList<>();
    private static final WebServersManager webServersManager = new WebServersManager();
    //TODO Locks atencao a acessos por diferentes threads

    private WebServersManager() {
        init();
        addExistingInstances();
    }

    public synchronized static WebServersManager getInstance() {
        return webServersManager;
    }

    public synchronized void incrementCounter(){
        requestCounter++;
        if(requestCounter == NUMBER_OF_REQUESTS_BETWEEN_UPDATES){
            requestCounter = 0;
            Thread t = new Thread(new UpdateEstimativeTask());
            t.start();
        }
    }

    //TODO isto devia ser
    public class UpdateEstimativeTask implements Runnable {

        @Override
        public void run() {
            Map<String, AttributeValue> values = new HashMap<>();
            values.put(":zero", new AttributeValue().withN(String.valueOf(0)));
            PaginatedScanList<MetricsStorage> metrics = Storage.getRequestMetricsToProcess();
            if(metrics.isEmpty() || metrics == null){
                return;
            }
            else {
                for(MetricsStorage metric : metrics){
                    EstimateStorage estimative = new EstimateStorage(metric.getRequestAttribute());
                    double mpa = metric.getMethodsNumber()/estimative.getArea();
                    estimative.setMethodsPerArea(mpa);
                    Storage.removeMetric(metric);
                }
            }

        }
    }

    public int getNumServer(){
        return webServers.size();
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
    public WebServerWrapper getWebServer(long requestCost) {
        //choosing instance with less "work" TODO see if this is to simple, maybe if progress is incorporate
        WebServerWrapper server = getServerWithLeastWork();
        return server;
    }

    private static WebServerWrapper getServerWithLeastWork() {
        long minCost = Long.MAX_VALUE;
        WebServerWrapper minWebServer = null;
        long thisCost;
        for (WebServerWrapper server : webServers) {
            thisCost = server.getCost();
            //TODO this is new not tested, about the running one
            if (thisCost < minCost && server.get_state() == WebServerWrapper.State.RUNNING) {
                minWebServer = server;
                minCost = thisCost;
            } else if (thisCost == minCost) {
                //TODO
            }
        }
        return minWebServer;
    }


    private void addWebServer(String instanceId) {
        webServers.add(new WebServerWrapper(instanceId));
    }

    public void stopLeastWorking(){
        WebServerWrapper minWebServer = getServerWithLeastWork();
        minWebServer.shutdownGracefully();

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
            String imageId;
            String instanceType;
            String state;
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
                            imageId = instance.getImageId(),
                            instanceType = instance.getInstanceType(),
                            state = instance.getState().getName(),
                            instance.getMonitoring().getState(),
                            ipAddress = instance.getPublicIpAddress());
                    if(imageId.equals("ami-0c80afef49b12a338") && instanceType.equals("t2.micro") && !state.equals(InstanceStateName.Terminated.toString()) && !state.equals(InstanceStateName.ShuttingDown.toString())) {
                        webServers.add(new WebServerWrapper(id,  ipAddress));
                    }
                }
            }

            request.setNextToken(response.getNextToken());

            if(response.getNextToken() == null) {
                done = true;
            }
        }
        if(webServers.isEmpty()) {
            createNewWebServer();
        }
    }

    public ArrayList<Double> getWSFullness(){
        ArrayList<Double> wsFullness = new ArrayList<>();
        for(WebServerWrapper ws : webServers){
            wsFullness.add(ws.getWorkFullness());
        }
        return wsFullness;
    }

    public void removeWebServer(WebServerWrapper ws){
        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(ws.get_id());
        ec2.terminateInstances(request);
        webServers.remove(ws);
    }
}