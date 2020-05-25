package org.radarbase.redcap.webapp.exception

import org.slf4j.LoggerFactory
import java.io.IOException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class RedcapOperationException @JvmOverloads constructor(
    private val msg: String = "",
    private val throwable: Throwable? = null
) :
    IOException(msg, throwable), ExceptionMapper<RedcapOperationException> {

    override fun toResponse(exception: RedcapOperationException?): Response {
        logger.error("Error has occurred while operating on the redcap instance. 500", exception)
        return Response.status(500).entity(
            "An Error has occurred while operating on the redcap instance. ${exception?.msg}" +
                    "\n${exception?.throwable}"
        ).type("text/plain").build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RedcapOperationException::class.java)
    }
}