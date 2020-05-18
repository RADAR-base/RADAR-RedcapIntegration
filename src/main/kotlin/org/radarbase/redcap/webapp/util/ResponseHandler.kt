package org.radarbase.redcap.webapp.util

import org.slf4j.LoggerFactory
import java.net.URI
import javax.ws.rs.core.Response

/*
 * Copyright 2016 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Generic response handler.
 */
object ResponseHandler {
    private val LOGGER = LoggerFactory.getLogger(ResponseHandler::class.java)

    /**
     * Response generator in case of success.
     * @param uri [URI] that has generated the computation
     * @return [Response] sent back to the API consumer
     */
    fun response(uri: URI): Response {
        val status = Response.Status.OK
        LOGGER.info("[{}] {}", status.statusCode, uri)
        return Response.status(status.statusCode).entity("").build()
    }
}