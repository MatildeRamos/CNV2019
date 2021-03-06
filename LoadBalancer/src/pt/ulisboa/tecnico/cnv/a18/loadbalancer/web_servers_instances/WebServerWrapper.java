package pt.ulisboa.tecnico.cnv.a18.loadbalancer.web_servers_instances;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.cnv.a18.storage.Request;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class WebServerWrapper {
    enum State {
        PENDING, RUNNING, STOPPING, STOPPED, SHUTTING_DOWN, TERMINATED
    }

    //TODO
    private final long _maxLoad = 100000L;
    private State _state;
    private String _id;
    private String _address;
    private long _totalCost = 0L;
    private HashMap<Request, Long> _currentRequests = new HashMap<>();

    private Timer t1;


    WebServerWrapper(String id, String ip) {
        _id = id;
        _state = State.RUNNING;
        setAddress(ip);
    }

    WebServerWrapper(String id) {
        _id = id;
        _state = State.PENDING;
        //Periodically check if the instance is already running
        TimerTask hasStartedCheck = new TimerTask() {
            @Override
            public void run() {
                AmazonEC2 ec2 = WebServersManager.ec2;

                DescribeInstancesRequest request = new DescribeInstancesRequest();
                request.withInstanceIds(_id);

                DescribeInstancesResult response = ec2.describeInstances(request);

                String instanceId = "";
                String instanceImage = "";
                String instanceType = "";
                String state = "";
                String monitoringState = "";
                String ipAddress = "";
                for(Reservation reservation : response.getReservations()) {
                    for(Instance instance : reservation.getInstances()) {
                        instanceId = instance.getInstanceId();
                        instanceImage = instance.getImageId();
                        instanceType = instance.getInstanceType();
                        state = instance.getState().getName();
                        monitoringState = instance.getMonitoring().getState();
                        ipAddress = instance.getPublicIpAddress();
                    }
                }
                if(state.equals(InstanceStateName.Running.toString()) && ipAddress != null) {
                    System.out.printf(
                            "Created instance with id %s, " +
                                    "AMI %s, " +
                                    "type %s, " +
                                    "state %s " +
                                    "monitoring state %s " +
                                    "and ip address %s\n",
                            instanceId,
                            instanceImage,
                            instanceType,
                            state,
                            monitoringState,
                            ipAddress);
                    _state = State.RUNNING;
                    setAddress(ipAddress);
                    t1.cancel();
                }
            }
        };
        t1 = new Timer();
        t1.schedule(hasStartedCheck, 0,10000);
    }

    public synchronized void shutdownGracefully(){
        _state = State.SHUTTING_DOWN;
        if(_totalCost == 0){
            WebServersManager.getInstance().removeWebServer(this);
        }
    }

    public String get_id(){
        return _id;
    }
    private void setAddress(String ip) {
        _address = ip + ":8000"; //TODO get the port value from properties file
    }

    public String getAddress() {
        return _address;
    }

    public void incrementCost(long cost) {
        _totalCost += cost;
    }

    public void decrementCost(long cost) {
        _totalCost -= cost;
    }

    long getCost() {
        return _totalCost;
    }

    public State get_state() {
        return _state;
    }

    public void set_state(State _state) {
        this._state = _state;
    }

    public void endRequest(Request request) {
        _totalCost -= _currentRequests.get(request);
        _currentRequests.remove(request);
        if(_state == State.SHUTTING_DOWN && _totalCost == 0){
            WebServersManager.getInstance().removeWebServer(this);
        }
    }

    public void addRequest(Request request, long cost) {
        _currentRequests.put(request, cost);
        _totalCost += cost;
    }

    public Double getWorkFullness(){
        Long l = _maxLoad;
        return (_totalCost/l.doubleValue())*100;
    }
}