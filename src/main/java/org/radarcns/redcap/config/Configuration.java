package org.radarcns.redcap.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URL;
import java.util.Set;

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
 * Java class that defines the configuration required by the web app to handle authentication and
 *      authorisation against Management Portal and REDCap instances.
 */
public class Configuration {

    @JsonProperty("oauth_client_id")
    private final String oauthClientId;

    @JsonProperty("oauth_client_secret")
    private final String oauthClientSecret;

    @JsonProperty("management_portal_url")
    private final URL managementPortalUrl;

    @JsonProperty("token_endpoint")
    private final String tokenEndpoint;

    @JsonProperty("project_endpoint")
    private final String projectEndpoint;

    @JsonProperty("subject_endpoint")
    private final String subjectEndpoint;

    private final Set<RedCapInfo> servers;

    /**
     * Constructor.
     * @param oauthClientId {@link String} representing OAuth2 client identifier
     * @param oauthClientSecret {@link String} representing OAuth2 client identifier
     * @param managementPortalUrl {@link URL} pointing a Management Portal instane
     * @param tokenEndpoint {@link String} representing Management Portal web root to renew tokens
     * @param projectEndpoint {@link String} representing Management Portal web root to access
     *      project data
     * @param subjectEndpoint {@link String} representing Management Portal web root to manage
     *      subject
     * @param servers {@link Set} of {@link RedCapInfo} providing information about REDCap instances
     */
    public Configuration(String oauthClientId, String oauthClientSecret,
            URL managementPortalUrl, String tokenEndpoint, String projectEndpoint,
            String subjectEndpoint, Set<RedCapInfo> servers) {
        this.oauthClientId = oauthClientId;
        this.oauthClientSecret = oauthClientSecret;
        this.managementPortalUrl = managementPortalUrl;
        this.tokenEndpoint = tokenEndpoint;
        this.projectEndpoint = projectEndpoint;
        this.subjectEndpoint = subjectEndpoint;
        this.servers = servers;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public URL getManagementPortalUrl() {
        return managementPortalUrl;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getProjectEndpoint() {
        return projectEndpoint;
    }

    public String getSubjectEndpoint() {
        return subjectEndpoint;
    }

    public Set<RedCapInfo> getServers() {
        return servers;
    }
}
