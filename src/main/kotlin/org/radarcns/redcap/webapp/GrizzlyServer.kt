package org.radarcns.redcap.webapp

import org.glassfish.hk2.utilities.ServiceLocatorUtilities
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.slf4j.LoggerFactory
import java.net.URI

object GrizzlyServer {
    private val logger = LoggerFactory.getLogger(GrizzlyServer::class.java)
    private const val BASE_URI = "http://0.0.0.0:8080/redcap/"

    /**
     * Main command line server.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val locator = ServiceLocatorUtilities.createAndPopulateServiceLocator()
        val httpServer = GrizzlyHttpServerFactory
            .createHttpServer(
                URI.create(BASE_URI),
                Application(),
                locator
            )
        // TODO Add an exception mapper
        try {
            httpServer.start()
            logger.info("Jersey app started on {}.\nPress Ctrl+C to stop it...", BASE_URI)
            Thread.currentThread().join()
        } catch (e: Exception) {
            logger.error("Error starting server", e)
        }
    }
}