package pt.ulisboa.tecnico.cnv.a18.loadbalancer.handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ulisboa.tecnico.cnv.a18.loadbalancer.web_servers_instances.WebServerWrapper;
import pt.ulisboa.tecnico.cnv.a18.loadbalancer.web_servers_instances.WebServersManager;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

public class RequestsHandler implements HttpHandler {

	private WebServersManager serversManager = WebServersManager.getInstance();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        // Get the query.
        final String query = httpExchange.getRequestURI().getQuery();

        //TODO aqui avaliar a situacao de modo a saber para que instancia redirecionar o pedido (metricas e cenas)
        int requestCode = 1;

        byte[] response = redirectRequest(query, requestCode);


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

    //TODO what should this return, as to do with what we will be doing from here on
    private byte[] redirectRequest(String request, int requestCost) {
		try {
			//Create connection with the chosen ec2 webServer instance
			//TODO receber um custo para o request, e escolher a vm para onde o enviar consoante o custo dos pedidos que ela já estiver a executar
			WebServerWrapper server = serversManager.getWebServer(requestCost); //TODO send a cost of the request and then the best fitted server will be chosen
			//TODO idle time do load balancer (esperar x tempo por resposta, passado esse tempo - a vm deve ter morrido - reenviar o pedido para outra vm)

			URL url = new URL("http://" + server.getAddress() + "/climb?" + request);
			System.out.println("Sending to ec2 WebServer > Query:\t" + url.toString());
            //TODO maybe we should store information to know that an instance is calculating the request's response
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("Request_ID", UUID.randomUUID().toString()); //Unique identifier for the request
			con.setRequestMethod("GET");



			System.out.println("Waiting for response...");

			//Read response from instance that have done de work
			DataInputStream inputStream = new DataInputStream(con.getInputStream());
			byte[] buffer = new byte[con.getContentLength()];
			inputStream.readFully(buffer);

			System.out.println("Response received");

			//TODO take note that the web server instance has already ended the request and returned the response

			return buffer;

		} catch(IOException e) {
			e.printStackTrace(); //TODO do something usefull here
		}
		return null; //TODO what should happen here?
    }
}