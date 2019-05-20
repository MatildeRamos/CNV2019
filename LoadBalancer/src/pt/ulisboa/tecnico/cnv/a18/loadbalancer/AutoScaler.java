package pt.ulisboa.tecnico.cnv.a18.loadbalancer;

import pt.ulisboa.tecnico.cnv.a18.loadbalancer.web_servers_instances.WebServersManager;

import java.util.Timer;
import java.util.TimerTask;

public class AutoScaler implements Runnable {

    private WebServersManager webServersManager = WebServersManager.getInstance();
    private Timer _statusCheckerTimer = new Timer();

    private int MIN_NUMBER_INSTANCES = 2;
    private int MAX_NUMBER_INSTANCES = 15;

    @Override
    public void run() {
        //TODO check this out
        _statusCheckerTimer.schedule(new StatusCheckerTask(), 0, 30000); //TODO value to think about
    }

    //TODO Probably act as health checker
    class StatusCheckerTask extends TimerTask {

        @Override
        public void run() {

        }
    }
}