package pt.ulisboa.tecnico.cnv.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.a18.storage.Request;
import pt.ulisboa.tecnico.cnv.a18.storage.RequestParser;
import pt.ulisboa.tecnico.cnv.a18.storage.db.AbstractStorage;
import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;

import javax.imageio.ImageIO;

public class WebServer {

	public static void main(final String[] args) throws Exception {

		//final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8001), 0);

		final HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);



		server.createContext("/climb", new MyHandler());
		server.createContext("/checkHealth", new MyHealthHandler());

		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println(server.getAddress().toString());
	}

	static class MyHealthHandler implements HttpHandler {
		@Override
			public void handle(HttpExchange t) throws IOException {
					String response = "This was the query:" + t.getRequestURI().getQuery() 
														 + "##";
					t.sendResponseHeaders(200, response.length());
					OutputStream os = t.getResponseBody();
					os.write(response.getBytes());
					os.close();
			}
	}


	static class MyHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {

			// Get the query.
			final String query = t.getRequestURI().getQuery();

			System.out.println("> Query:\t" + query);

			// Break it down into String[].
			final String[] params = query.split("&");


			for(String p: params) {
				System.out.println(p);
			}

			String requestId = t.getRequestHeaders().getFirst("Request_ID");
			Request request = new RequestParser(query).parseRequest(requestId);
			AbstractStorage.getStorage().setNewRequest(Long.parseLong(requestId), request);


			// Store as if it was a direct call to SolverMain.
			final ArrayList<String> newArgs = new ArrayList<>();
			for (final String p : params) {
				final String[] splitParam = p.split("=");
				newArgs.add("-" + splitParam[0]);
				newArgs.add(splitParam[1]);

				/*
				System.out.println("splitParam[0]: " + splitParam[0]);
				System.out.println("splitParam[1]: " + splitParam[1]);
				*/
			}

			newArgs.add("-d");

			// Store from ArrayList into regular String[].
			final String[] args = new String[newArgs.size()];
			int i = 0;
			for(String arg: newArgs) {
				args[i] = arg;
				i++;
			}

			/*
			for(String ar : args) {
				System.out.println("ar: " + ar);
			} */

			SolverArgumentParser ap = null;
			try {
				// Get user-provided flags.
				ap = new SolverArgumentParser(args);
			}
			catch(Exception e) {
				System.out.println(e);
				return;
			}

			System.out.println("> Finished parsing args.");


			// Create solver instance from factory.
			final Solver s = SolverFactory.getInstance().makeSolver(ap);

			BufferedWriter writer = new BufferedWriter(new FileWriter("Metrics_T" + Thread.currentThread().getId() + ".txt", true));
			writer.append("x0: " + ap.getX0() + "\n");
			writer.append("y0: " + ap.getY1() + "\n");
			writer.append("x1: " + ap.getX1() + "\n");
			writer.append("y1: " + ap.getY1() + "\n");
			writer.append("s0: " + ap.getStartX() + "\n");
			writer.append("s1: " + ap.getStartY() + "\n");
			writer.append("input image: " + ap.getInputImage() + "\n");
			writer.append("strategy: " + ap.getSolverStrategy() + "\n");
			writer.close();

			// Write figure file to disk.
			File responseFile = null;
			try {

				final BufferedImage outputImg = s.solveImage();

				final String outPath = ap.getOutputDirectory();

				final String imageName = s.toString();

				if(ap.isDebugging()) {
					System.out.println("> Image name: " + imageName);
				}

				final Path imagePathPNG = Paths.get(outPath, imageName);
				ImageIO.write(outputImg, "png", imagePathPNG.toFile());

				responseFile = imagePathPNG.toFile();

			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}



			// Send response to browser.
			final Headers hdrs = t.getResponseHeaders();

			t.sendResponseHeaders(200, responseFile.length());

			hdrs.add("Content-Type", "image/png");

			hdrs.add("Access-Control-Allow-Origin", "*");
			hdrs.add("Access-Control-Allow-Credentials", "true");
			hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
			hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

			final OutputStream os = t.getResponseBody();
			Files.copy(responseFile.toPath(), os);


			os.close();

			System.out.println("> Sent response to " + t.getRemoteAddress().toString());
		}
	}

}
