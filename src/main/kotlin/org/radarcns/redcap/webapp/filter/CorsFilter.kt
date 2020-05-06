package org.radarcns.redcap.webapp.filter

import javax.annotation.Priority
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider

/**
 * Standard CORS filter, applied to all responses.
 */
@Provider
@Priority(500)
class CorsFilter : ContainerResponseFilter {
    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext
    ) {
        val headers = responseContext.headers
        headers.add("Access-Control-Allow-Origin", "*")
        headers.add("Access-Control-Allow-Headers", "origin, content-type, accept")
        headers.add("Access-Control-Allow-Credentials", "true")
        headers.add("Access-Control-Allow-Methods", "POST, OPTIONS, HEAD")
        headers.add("Access-Control-Max-Age", "1209600")
    }
}