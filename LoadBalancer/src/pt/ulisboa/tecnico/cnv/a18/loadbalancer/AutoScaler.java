package pt.ulisboa.tecnico.cnv.a18.loadbalancer;

import pt.ulisboa.tecnico.cnv.a18.loadbalancer.web_servers_instances.WebServersManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AutoScaler implements Runnable {

    private WebServersManager webServersManager = WebServersManager.getInstance();
    private Timer _statusCheckerTimer = new Timer();

    private static int MIN_NUMBER_INSTANCES = 1; //TODO properties file
    private static int MAX_NUMBER_INSTANCES = 15; //TODO properties file
    private static int SEC_OVERLOAD = 30; //TODO properties file
    private static int SEC_UNDERLOAD = 30; //TODO properties file
    private static int HIGH_TRESHHOLD = 80; //TODO properties file
    private static int LOW_TRESHHOLD = 30; //TODO properties file

    protected static int timeStampsOverload = 0;
    protected static int timeStampsUnderload = 0;


    private static final ScalingPolicy increaseGroupSize = new ScalingPolicy(MIN_NUMBER_INSTANCES, SEC_OVERLOAD, HIGH_TRESHHOLD);
    private static final ScalingPolicy decreaseGroupSize = new ScalingPolicy(MAX_NUMBER_INSTANCES, SEC_UNDERLOAD, LOW_TRESHHOLD);

    @Override
    public void run() {
        //TODO check this out
        _statusCheckerTimer.schedule(new StatusCheckerTask(), 0, 1000); //1 em 1 seg, dps o contador ate o sec overload
    }

    public static int calcOverTresh(ArrayList<Double> wsFullness){
        int res = 0;
        for(Double d : wsFullness){
            if(d > increaseGroupSize.get_loadThreshold()){
                res++;
            }
        }
        return res;
    }

    public static int calcUnderTresh(ArrayList<Double> wsFullness){
        int res = 0;
        for(Double d : wsFullness){
            if(d < decreaseGroupSize.get_loadThreshold()){
                res++;
            }
        }
        return res;
    }

    //TODO Probably act as health checker
    class StatusCheckerTask extends TimerTask {

        @Override
        public void run() {
            if(webServersManager.getNumServer() <= increaseGroupSize.get_numServers() ){
                webServersManager.createNewWebServer();
            }
            else {
                if(webServersManager.getNumServer() >= decreaseGroupSize.get_numServers()){
                    //webServersManager.shutdownInstance();
                }
                else {
                    ArrayList wsFullness = webServersManager.getWSFullness();
                    if(AutoScaler.calcOverTresh(wsFullness) >= webServersManager.getNumServer()-1 ){
                        AutoScaler.timeStampsOverload++;
                    }
                    else if(AutoScaler.calcUnderTresh(wsFullness) <= webServersManager.getNumServer() -1 ){
                        AutoScaler.timeStampsUnderload++;
                    }
                    else {
                        AutoScaler.timeStampsOverload = 0;
                        AutoScaler.timeStampsUnderload = 0;
                    }
                    if(AutoScaler.timeStampsOverload >= (increaseGroupSize.get_secondsOverThreshold()/1000)){
                        webServersManager.createNewWebServer();
                        AutoScaler.timeStampsOverload = 0;
                    }
                    else if(AutoScaler.timeStampsUnderload >= (decreaseGroupSize.get_secondsOverThreshold()/1000)){
                        //webServersManager.shutdownInstance();
                        AutoScaler.timeStampsUnderload = 0;
                    }
                }
            }
        }
    }
}