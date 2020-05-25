package org.radarbase.redcap.webapp.exception

import org.radarcns.exception.TokenException
import org.slf4j.LoggerFactory
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class UncaughtTokenException : TokenException(), ExceptionMapper<TokenException> {

    override fun toResponse(exception: TokenException?): Response {
        logger.error("Error getting token from Management portal. Unauthorized! 401", exception)
        return Response.status(401).entity(
            "Error getting token from Management portal. ${exception?.message}" +
                    "\n${exception?.cause}"
        ).type("text/plain").build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UncaughtTokenException::class.java)
    }
}