package org.radarcns.redcap.webapp.exception

import java.io.IOException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class UncaughtIOException : IOException(), ExceptionMapper<IOException> {

    override fun toResponse(exception: IOException?): Response =
        Response.status(500).entity(
            "An I/O Exception has occurred while processing the request. ${exception?.message}" +
                    "\n${exception?.cause}"
        ).type("text/plain").build()
}