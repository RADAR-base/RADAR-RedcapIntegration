package org.radarbase.redcap.webapp.exception

import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class IllegalRequestException @JvmOverloads constructor(
    private val msg: String = "",
    private val throwable: Throwable? = null
) :
    IllegalStateException(msg, throwable), ExceptionMapper<IllegalRequestException> {

    override fun toResponse(exception: IllegalRequestException?): Response {
        logger.error("Bad Request! 400", exception)
        return Response.status(400).entity(
            "The request was not successful. Please verify its validity. ${exception?.msg}" +
                    "\n${exception?.throwable}"
        ).type("text/plain").build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IllegalRequestException::class.java)
    }
}