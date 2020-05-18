package org.radarbase.redcap.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

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
 * Java class that defines the configuration required by the web app to handle authentication and
 * authorisation against Management Portal and REDCap instances.
 *
 * Current implementation support a single Management Portal instance since the current
 * RADAR-CNS Platform Architecture is designed with a centralised Management Portal. In order
 * to support multiple Management Portal instances, the following variables
 *  * `oauthClientId`
 *  * `oauthClientSecret`
 *  * `managementPortalUrl`
 *  * `tokenEndpoint`
 *  * `projectEndpoint`
 *  * `subjectEndpoint`
 *
 * should be moved to [ManagementPortalInfo].
 */
data class Configuration
/**
 * Constructor.
 * @param version [String] reporting the web app current version
 * @param released [String] reporting the web app released date
 * @param oauthClientId [String] representing OAuth2 client identifier
 * @param oauthClientSecret [String] representing OAuth2 client identifier
 * @param managementPortalUrl [URL] pointing a Management Portal instane
 * @param tokenEndpoint [String] representing Management Portal web root to renew tokens
 * @param projectEndpoint [String] representing Management Portal web root to access
 * project data
 * @param subjectEndpoint [String] representing Management Portal web root to manage
 * subject
 * @param projects [Set] of [ProjectInfo] providing information about REDCap and
 * Management Portal instances
 */ @JsonCreator constructor(
    /** Service version.  */
    @JsonProperty("version") val version: String,
    /** Release date.  */
    @JsonProperty("released") val released: String,
    /** OAuth2 client identifier.  */
    @JsonProperty("oauth_client_id") val oauthClientId: String,
    /** OAuth2 client secret.  */
    @JsonProperty("oauth_client_secret") val oauthClientSecret: String,
    /** URL pointing a Management Portal instance.  */
    @JsonProperty("management_portal_url") val managementPortalUrl: URL,
    /** Web root of Management Portal token end point. It is required to refresh Access Token.  */
    @JsonProperty("token_endpoint") val tokenEndpoint: String,
    /**
     * Web root of Management Portal project end point. It is required to get a Management Portal
     * Project.
     */
    @JsonProperty("project_endpoint") val projectEndpoint: String,
    /**
     * Web root of Management Portal subject end point. It is required to create and get Managemen
     * Portal Subjects.
     */
    @JsonProperty("subject_endpoint") val subjectEndpoint: String,
    /**
     * Set of supported projects. For each project REDCap and Management Portal configurations
     * are set.
     */
    @JsonProperty("projects") val projects: Set<ProjectInfo>
)