package org.radarcns.redcap.webapp;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class GrizzlyServer {

    private static final Logger logger = LoggerFactory.getLogger(GrizzlyServer.class);
    private static final String BASE_URI = "http://0.0.0.0:8080/redcap/";

    /**
     * Main command line server.
     */
    public static void main(String[] args) {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();

        HttpServer httpServer = GrizzlyHttpServerFactory
                .createHttpServer(URI.create(BASE_URI), new Application(), locator);

        // TODO Add an exception mapper
        try {
            httpServer.start();

            System.out.println(String.format("Jersey app started on %s.\nPress Ctrl+C to stop it...",
                    BASE_URI));
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Error starting server", e);
        }
    }
}
