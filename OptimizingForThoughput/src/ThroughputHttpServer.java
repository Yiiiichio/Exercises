import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//This is an example of a http server with constant number of threads serving a flow of http requests

//It will first load a very large book from resources folder.
//It will act like a very basic search engine - the client will send us a word in http request url,
//and the application will search for that word in the book and count how many times that word would appears
//in the novel as a response which the application will send to the user.

//to test this application, open the browser, go to localhost:8000/search?keywordToSearch


public class ThroughputHttpServer {
    private static final String INPUT_FILE = "./resources/war_and_peace.txt";
    private static final int NUMBER_OF_THREADS = 8;

    public static void main(String[] args) throws IOException {
        //read the book from the file to String
        String text = new String(Files.readAllBytes(Paths.get(INPUT_FILE)));
        startServer(text);
    }

    public static void startServer(String text) throws IOException {
        //create http server, choose port to be 8000 to be listened on
        //the second parameter is backlog size which defines the size of the queue for http server requests
        //leave it zero since all the requests should end up in the thread pool's queue instead
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        //create the context which basically assigns a handler object to a particular HTTP route
        server.createContext("/search", new WordCountHandler(text));

        //create a fix thread pool
        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        server.setExecutor(executor);

        //after start() method is called, the application will start to listening to HTTP requests on port 8000
        //All the requests that belongs to the "search" route will be handled by this WordCountHandler
        //and executed on one of the thread in the pool.
        server.start();
    }

    private static class WordCountHandler implements HttpHandler {
        private String text;

        public WordCountHandler(String text) {
            this.text = text;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String query = httpExchange.getRequestURI().getQuery();
            String[] keyValue = query.split("=");
            String action = keyValue[0];
            String word = keyValue[1];
            if (!action.equals("word")) {
                httpExchange.sendResponseHeaders(400, 0);
                return;
            }

            long count = countWord(word);

            byte[] response = Long.toString(count).getBytes();
            httpExchange.sendResponseHeaders(200, response.length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response);
            outputStream.close();
        }

        private long countWord(String word) {
            long count = 0;
            int index = 0;
            while (index >= 0) {
                index = text.indexOf(word, index);

                if (index >= 0) {
                    count++;
                    index++;
                }
            }
            return count;
        }
    }
}