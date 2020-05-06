package org.radarcns.redcap.webapp;

import okhttp3.OkHttpClient;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.radarcns.redcap.listener.HttpClientFactory;
import org.radarcns.redcap.managementportal.MpClient;
import org.radarcns.redcap.webapp.filter.CorsFilter;

import javax.inject.Singleton;

/**
 * Application configuration.
 *
 * <p>Replaces previous {@code web.xml}.
 */
class Application extends ResourceConfig{

    Application() {
        packages("org.radarcns.redcap.webapp.resource");

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(HttpClientFactory.class)
                        .to(OkHttpClient.class)
                        .in(Singleton.class);

                bind(MpClient.class)
                        .to(MpClient.class)
                        .in(Singleton.class);
            }
        });

        register(CorsFilter.class);
    }
}
