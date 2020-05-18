package org.radarbase.redcap.webapp.resource

import org.radarbase.redcap.config.RedCapManager
import org.radarbase.redcap.integration.Integrator
import org.radarbase.redcap.managementportal.MpClient
import org.radarbase.redcap.util.RedCapTrigger
import org.radarbase.redcap.webapp.exception.IllegalRequestException
import org.radarbase.redcap.webapp.exception.RedcapOperationException
import org.radarbase.redcap.webapp.util.PathLabels
import org.radarbase.redcap.webapp.util.ResponseHandler.response
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.inject.Inject
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo

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
/**
 *
 * Entry point hits by REDCap trigger functionality.
 *
 * In the REDCap Project Setup view, under `Additional customisations`, it is possible
 * to set a `Data Entry Trigger`. Upon any form/instrument or survey creation or update,
 * REDCap automatically triggers a request to the set end point.
 *
 * @see RedCapTrigger
 */
@Path("/" + PathLabels.REDCAP_TRIGGER)
class EntryPoint @Inject constructor(private val mpClient: MpClient) {

    /**
     * HTTP POST request handler. This function trigger a subject creation in the Management
     * Portal in case the event related to the form involved in the update is the enrolment.
     * In case this function fails, REDCap will work without any problem. The REDCap's log does
     * not report anything about the return of this function. Only the updates are logged in.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun handlerPostRequest(@Context ui: UriInfo, body: String?): Response {
        return try {
            if (body == null || body.isEmpty()) {
                throw IllegalRequestException(
                    "The body in the POST request " +
                            "cannot be empty or null."
                )
            }
            val trigger = RedCapTrigger(body)
            if (!RedCapManager.isSupportedInstance(trigger)) {
                throw IllegalRequestException(
                    "Requests coming from ${trigger.redcapUrl} for " +
                            "project Id ${trigger.projectId} cannot be managed."
                )
            }
            if (trigger.isEnrolment) {
                val enrolment = Integrator(
                    trigger,
                    mpClient
                )
                return if (enrolment.handleDataEntryTrigger()) {
                    response(ui.requestUri)
                } else {
                    throw RedcapOperationException(
                        "Redcap From update was not successful."
                    )
                }
            } else {
                LOGGER.info(
                    "[{}] Skip trigger from {} instrument \"{}\" upon event \"{}\".",
                    trigger.projectId, trigger.redcapUrl, trigger.instrument,
                    trigger.redcapEventName
                )
                response(ui.requestUri)
            }
        } catch (exc: Exception) {
            throw IOException("URI ${ui.requestUri} request failed.", exc)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EntryPoint::class.java)
    }
}