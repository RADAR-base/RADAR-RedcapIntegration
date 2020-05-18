package org.radarcns.redcap.webapp.exception

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

    override fun toResponse(exception: RedcapOperationException?): Response =
        Response.status(500).entity(
            "An Error has occurred while operating on the redcap instance. ${exception?.msg}" +
                    "\n${exception?.throwable}"
        ).type("text/plain").build()
}