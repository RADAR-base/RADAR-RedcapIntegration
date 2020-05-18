package org.radarbase.redcap.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

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
 * Defines the configuration required by the web app to handle authentication and
 * authorisation against Management Portal and REDCap instances.
 *
 * Current implementation support a single Management Portal instance since the current
 * RADAR-Base Platform Architecture is designed with a centralised Management Portal. In order
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
 * @param mpConfig the [ManagementPortalConfig]
 * @param projects [Set] of [ProjectInfo] providing information about REDCap and
 * Management Portal instances
 */
@JsonCreator constructor(
    @JsonProperty("management_portal") val mpConfig: ManagementPortalConfig,
    @JsonProperty("projects") val projects: Set<ProjectInfo>
)