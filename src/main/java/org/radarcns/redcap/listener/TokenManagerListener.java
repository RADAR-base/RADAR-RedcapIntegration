package org.radarcns.redcap.listener;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.radarcns.redcap.config.Properties;
import org.radarcns.redcap.util.RefreshToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refreshes the OAuth2 token needed to authenticate against the Management Portal and adds it to
 *      the {@link javax.servlet.ServletContext} in this way multiple function can make reuse of it.
 */
@WebListener
public class TokenManagerListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenManagerListener.class);

    private static final String ACCESS_TOKEN = "TOKEN";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String SCOPE_HEADER = "scope";

    private static final OkHttpClient client;
    private static final Request request;

    private static long tokenDuration;
    private static long lastRefresh;

    static {
        client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .authenticator((route, response) -> response.request().newBuilder()
                    .header(AUTHORIZATION_HEADER, Properties.getOauthCredential())
                    .build()
            ).build();

        try {
            request = new Request.Builder()
                .url(Properties.getTokenEndPoint())
                .post(new FormBody.Builder().add("grant_type", "client_credentials")
                    //TODO check if scope is correct. This token must be able to access projects
                    // and create users
                    .add(SCOPE_HEADER, "read")
                    .build())
                .build();
        } catch (MalformedURLException exc) {
            LOGGER.error("Properties cannot be load. Check the log for more information.", exc);
            throw new ExceptionInInitializerError(exc);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            getToken(sce.getServletContext());
        } catch (IOException exc) {
            LOGGER.warn("{} cannot be generated.", ACCESS_TOKEN);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        client.connectionPool().evictAll();
        sce.getServletContext().setAttribute(ACCESS_TOKEN, null);

        LOGGER.info("{} has been invalidated.", ACCESS_TOKEN);
    }

    /**
     * Returns the {@code Access Token} needed to interact with the Management Portal. If the token
     *      available in {@link ServletContext} is still valid, it will be returned. In case it has
     *      expired, the functional will automatically renew it.
     * @param context {@link ServletContext} where the last used {@code Access Token} has been
     *      stored
     * @return a valid {@code Access Token} to contact Management Portal
     * @throws IOException in case the refresh cannot be completed
     */
    public static String getToken(ServletContext context) throws IOException {
        if (needRefresh()) {
            refresh(context);
        }

        String token = (String) context.getAttribute(ACCESS_TOKEN);

        if (Objects.isNull(token)) {
            throw new IllegalStateException("Access Token is null.");
        }

        return token;
    }

    private static boolean needRefresh() {
        return System.currentTimeMillis() - lastRefresh >= tokenDuration;
    }

    private static void refresh(ServletContext context) throws IOException {
        RefreshToken refreshToken = RefreshToken.getObject(client.newCall(request).execute());
        tokenDuration = refreshToken.getExpiresIn();
        lastRefresh = refreshToken.getIssueDate();

        context.setAttribute(ACCESS_TOKEN, refreshToken.getAccessToken());

        LOGGER.info("Refreshed token at {} valid till {}", getDate(lastRefresh),
                getDate(lastRefresh + tokenDuration));
    }

    private static String getDate(long time) {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS").format(new Date(time));
    }
}
