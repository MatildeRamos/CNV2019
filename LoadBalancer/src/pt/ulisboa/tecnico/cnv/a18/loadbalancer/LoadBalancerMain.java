package pt.ulisboa.tecnico.cnv.a18.loadbalancer;

public class LoadBalancerMain {

    public static void main(final String[] args) throws Exception {
        //Start all components of management
        Thread loadBalancerThread = new Thread(new LoadBalancer());
        loadBalancerThread.start();
        Thread autoScalerThread = new Thread(new AutoScaler());
        autoScalerThread.start();
    }
}