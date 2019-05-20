package pt.ulisboa.tecnico.cnv.a18.loadbalancer.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ulisboa.tecnico.cnv.a18.loadbalancer.storage.LBStorage;
import pt.ulisboa.tecnico.cnv.a18.loadbalancer.web_servers_instances.WebServerWrapper;
import pt.ulisboa.tecnico.cnv.a18.loadbalancer.web_servers_instances.WebServersManager;
import pt.ulisboa.tecnico.cnv.a18.storage.Request;
import pt.ulisboa.tecnico.cnv.a18.storage.RequestParser;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class RequestsHandler implements HttpHandler {

	private WebServersManager serversManager = WebServersManager.getInstance();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        // Get the query.
        final String query = httpExchange.getRequestURI().getQuery();

        String requestId = UUID.randomUUID().toString();

        Request newRequest = new RequestParser(query).parseRequest(requestId);

        //Calculate request Cost through estimations
        long requestCode = computeRequestCost(newRequest);//TODO tavam a falar de um objecto request, será um bom sitio para usalo I guess

        byte[] response = redirectRequest(query, requestCode, requestId, newRequest);

        // Send response to browser.
        httpExchange.sendResponseHeaders(200, response.length);

        final Headers hdrs = httpExchange.getResponseHeaders();
        hdrs.add("Content-Type", "image/png");
        hdrs.add("Access-Control-Allow-Origin", "*");
        hdrs.add("Access-Control-Allow-Credentials", "true");
        hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
        hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        final OutputStream os = httpExchange.getResponseBody();
        os.write(response);
        os.close();

        System.out.println("> Sent response to " + httpExchange.getRemoteAddress().toString());
    }

    private long computeRequestCost(Request request) {
        return LBStorage.getStorage().calcExpectedNumberofMethods(request);
    }

    //TODO what should this return, as to do with what we will be doing from here on
    private byte[] redirectRequest(String request, long requestCost, String requestId, Request req) {
		try {
			//Create connection with the chosen ec2 webServer instance
			WebServerWrapper server = serversManager.getWebServer(requestCost);
			//TODO idle time do load balancer (esperar x tempo por resposta, passado esse tempo - a vm deve ter morrido - reenviar o pedido para outra vm)

            //Add to the current cost of a server requests, the cost of the new request
            server.addRequest(req, requestCost);

			URL url = new URL("http://" + server.getAddress() + "/climb?" + request);
			System.out.println("Sending to ec2 WebServer > Query:\t" + url.toString());

			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestProperty("Request_ID", requestId); //Unique identifier for the request
			con.setRequestMethod("GET");

			System.out.println("Waiting for response...");

			//Read response from instance that have done de work
			DataInputStream inputStream = new DataInputStream(con.getInputStream());
			byte[] buffer = new byte[con.getContentLength()];
			inputStream.readFully(buffer);

			System.out.println("Response received");

            // Server has ended request remove the cost associated with the request
            server.endRequest(req);

			return buffer;

		} catch(IOException e) {
			e.printStackTrace(); //TODO do something usefull here
		}
		return null; //TODO what should happen here?
    }
}