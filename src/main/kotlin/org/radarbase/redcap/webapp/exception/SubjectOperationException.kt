package org.radarbase.redcap.webapp.exception

import org.slf4j.LoggerFactory
import java.io.IOException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class SubjectOperationException @JvmOverloads constructor(
    private val msg: String = "",
    private val throwable: Throwable? = null
) :
    IOException(msg, throwable), ExceptionMapper<SubjectOperationException> {

    override fun toResponse(exception: SubjectOperationException?): Response {
        logger.error("Error has occurred while operating on the subject. 500", exception)
        return Response.status(500).entity(
            "An Error has occurred while operating on the subject. ${exception?.msg}" +
                    "\n${exception?.throwable}"
        ).type("text/plain").build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SubjectOperationException::class.java)
    }
}