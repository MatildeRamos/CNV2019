package pt.ulisboa.tecnico.cnv.a18.loadbalancer;

import java.io.IOException;

import java.net.InetSocketAddress;

import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import pt.ulisboa.tecnico.cnv.a18.loadbalancer.handlers.RequestsHandler;

public class LoadBalancer implements Runnable {

    @Override
    public void run() {
        final HttpServer server;
        try {
            //TODO retrieve port from config.properties file
            //server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8000), 0);
            server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/climb", new RequestsHandler());

            // be aware! infinite pool of threads!
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();

            System.out.println(server.getAddress().toString());
        } catch (IOException e) {
            e.printStackTrace(); //TODO do something usefull here
        }
    }
}
