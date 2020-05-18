package org.radarbase.redcap.webapp.exception

import org.radarcns.exception.TokenException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class UncaughtTokenException : TokenException(), ExceptionMapper<TokenException> {

    override fun toResponse(exception: TokenException?): Response =
        Response.status(401).entity(
            "The request was not successful. Please verify its validity. ${exception?.message}" +
                    "\n${exception?.cause}"
        ).type("text/plain").build()
}