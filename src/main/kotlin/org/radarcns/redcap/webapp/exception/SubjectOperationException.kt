package org.radarcns.redcap.webapp.exception

import java.io.IOException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class SubjectOperationException(private val msg: String, private val throwable: Throwable? = null) :
    IOException(msg, throwable), ExceptionMapper<SubjectOperationException> {

    override fun toResponse(exception: SubjectOperationException?): Response =
        Response.status(500).entity(
            "An Error has occurred while operating on the subject. ${exception?.msg}" +
                    "\n${exception?.throwable}"
        ).type("text/plain").build()
}