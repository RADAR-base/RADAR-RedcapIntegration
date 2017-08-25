package org.radarcns.redcap.webapp;

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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic response handler.
 */
public class ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);

    /**
     * Response generator in case of success.
     * @param request {@link HttpServletRequest} that has generated the computation
     * @return {@link Response} sent back to the API consumer
     */
    public static Response getResponse(HttpServletRequest request) {
        Status status = Status.OK;

        LOGGER.info("[{}] {}", status.getStatusCode(), request.getRequestURI());

        return Response.status(status.getStatusCode()).entity("").build();
    }

    /**
     * Response generator in case of error.
     * @param request {@link HttpServletRequest} that has generated the computation
     * @return {@link Response} sent back to the API consumer
     */
    public static Response getErrorResponse(HttpServletRequest request) {
        LOGGER.error("[{}] {}", Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                request.getRequestURI());

        return Response.serverError().build();
    }
}
