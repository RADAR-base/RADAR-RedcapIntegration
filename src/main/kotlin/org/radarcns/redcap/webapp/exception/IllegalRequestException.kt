package org.radarcns.redcap.webapp.exception

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class IllegalRequestException(private val msg: String, private val throwable: Throwable? = null) :
    IllegalStateException(msg, throwable), ExceptionMapper<IllegalRequestException> {

    override fun toResponse(exception: IllegalRequestException?): Response =
        Response.status(400).entity(
            "The request was not successful. Please verify its validity. ${exception?.msg}" +
                    "\n${exception?.throwable}"
        ).type("text/plain").build()
}