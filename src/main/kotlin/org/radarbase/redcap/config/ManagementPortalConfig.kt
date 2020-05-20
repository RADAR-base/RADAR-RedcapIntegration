package org.radarbase.redcap.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

data class ManagementPortalConfig
/**
 * Constructor.
 * @param oauthClientId [String] representing OAuth2 client identifier
 * @param oauthClientSecret [String] representing OAuth2 client identifier
 * @param managementPortalUrl [URL] pointing a Management Portal instane
 * @param tokenEndpoint [String] representing Management Portal web root to renew tokens
 * @param projectEndpoint [String] representing Management Portal web root to access
 * project data
 * @param subjectEndpoint [String] representing Management Portal web root to manage
 * subject
 **/
@JsonCreator constructor(
    @JsonProperty("oauth_client_id") val oauthClientId: String,
    @JsonProperty("oauth_client_secret") val oauthClientSecret: String,
    @JsonProperty("base_url") val managementPortalUrl: URL,
    @JsonProperty("token_endpoint") val tokenEndpoint: String = "oauth/token",
    @JsonProperty("project_endpoint") val projectEndpoint: String = "api/projects/",
    @JsonProperty("subject_endpoint") val subjectEndpoint: String = "api/subjects/"
)