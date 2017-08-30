package org.radarcns.redcap.util;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import okhttp3.Response;

public class RefreshToken {

    @JsonProperty("access_token")
    private final String accessToken;

    @JsonProperty("token_type")
    private final String tokenType;

    @JsonProperty("expires_in")
    private final long expiresIn;

    @JsonProperty("scope")
    private final String scope;

    @JsonProperty("sub")
    private final String subject;

    @JsonProperty("iss")
    private final String issuer;

    @JsonProperty("iat")
    private final long issueDate;

    @JsonProperty("jti")
    private final String jsonWebTokenId;

    /**
     * Constructor.
     * @param accessToken {@link String} representing an Access Token
     * @param tokenType {@link String} defining the Token Type
     * @param expiresIn time in millisecond after which the token will be no longer valid
     * @param scope {@link String} defining the scope for using the token
     * @param subject {@link String} identifying the subject associated to the token
     * @param issuer {@link String} stating the identity that has issued the token
     * @param issueDate time in millisecond when the token has been issued
     * @param jsonWebTokenId {@link String} useful to validate the token
     */
    public RefreshToken(String accessToken, String tokenType, long expiresIn, String scope,
            String subject, String issuer, long issueDate, String jsonWebTokenId) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.scope = scope;
        this.subject = subject;
        this.issuer = issuer;
        this.issueDate = issueDate;
        this.jsonWebTokenId = jsonWebTokenId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getSubject() {
        return subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public long getIssueDate() {
        return issueDate;
    }

    public String getJsonWebTokenId() {
        return jsonWebTokenId;
    }

    public static RefreshToken getObject(Response response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.message(), RefreshToken.class);
    }
}
