package org.radarcns.redcap.webapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.radarcns.redcap.listener.HttpClientFactory
import org.radarcns.redcap.managementportal.MpClient
import org.radarcns.redcap.webapp.filter.CorsFilter
import javax.inject.Singleton
import javax.ws.rs.ext.ContextResolver

/**
 * Application configuration.
 *
 *
 * Replaces previous `web.xml`.
 */
internal class Application : ResourceConfig() {
    init {
        packages(
            "org.radarcns.redcap.webapp.resource",
            "org.radarcns.redcap.managementportal"
        )
        register(object : AbstractBinder() {
            override fun configure() {
                bindFactory(HttpClientFactory::class.java)
                    .to(OkHttpClient::class.java)
                    .`in`(Singleton::class.java)
                bind(MpClient::class.java)
                    .to(MpClient::class.java)
                    .`in`(Singleton::class.java)
            }
        })
        register(CorsFilter::class.java)
        register(ContextResolver<ObjectMapper> {
            ObjectMapper().registerModule(KotlinModule())
        })
    }
}