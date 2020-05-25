package org.radarbase.redcap.webapp.exception

import org.slf4j.LoggerFactory
import java.io.IOException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class UncaughtIOException : IOException(), ExceptionMapper<IOException> {

    override fun toResponse(exception: IOException?): Response {
        logger.error("An I/O Exception has occurred while processing the request. 500", exception)
        return Response.status(500).entity(
            "An I/O Exception has occurred while processing the request. ${exception?.message}" +
                    "\n${exception?.cause}"
        ).type("text/plain").build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UncaughtIOException::class.java)
    }
}