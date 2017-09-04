package org.radarcns.redcap;

/*
 * Copyright 2017 King's College London
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

//TODO improve README

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.radarcns.redcap.config.RedCapManager;
import org.radarcns.redcap.integration.Integrator;
import org.radarcns.redcap.listener.HttpClientListener;
import org.radarcns.redcap.util.RedCapTrigger;
import org.radarcns.redcap.util.RedCapUpdater;
import org.radarcns.redcap.webapp.PathLabels;
import org.radarcns.redcap.webapp.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Entry point hits by REDCap trigger functionality.</p>
 * <p>In the REDCap Project Setup view, under {@code Additional customisations}, it is possible
 * to set a {@code Data Entry Trigger}. Upon any form/instrument or survey creation or update,
 * REDCap automatically triggers a request to the set end point.</p>
 * @see RedCapTrigger
 */

@Path("/" + PathLabels.REDCAP_TRIGGER)
public class EntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);

    @Context
    private HttpServletRequest request;
    @Context
    private ServletContext context;

    /**
     * HTTP POST request handler. This function trigger a subject creation in the Management
     *      Portal in case the event related to the form involved in the update is the enrolment.
     *      In case this function fails, REDCap will work without any problem. The REDCap's log does
     *      not report anything about the return of this function. Only the updates are logged in.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response handlerPostRequest() {
        try {
            RedCapTrigger trigger = new RedCapTrigger(request);

            if (!RedCapManager.isSupportedInstance(trigger)) {
                LOGGER.error("Requests coming from " + trigger.getRedcapUrl() + " for project Id "
                        + trigger.getProjectId() + " cannot be managed.");
                return ResponseHandler.getErrorResponse(request);
            }

            if (trigger.isEnrolment()) {
                RedCapUpdater enrolment = new Integrator(trigger, context);

                if (enrolment.updateForm()) {
                    return ResponseHandler.getResponse(request);
                } else {
                    return ResponseHandler.getErrorResponse(request);
                }
            } else {
                LOGGER.debug("[{}] Skip trigger from {} instrument \"{}\" upon event \"{}\".",
                        trigger.getProjectId(), trigger.getRedcapUrl(), trigger.getInstrument(),
                        trigger.getRedcapEventName());
                return ResponseHandler.getResponse(request);
            }
        } catch (Exception exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getErrorResponse(request);
        }
    }

}