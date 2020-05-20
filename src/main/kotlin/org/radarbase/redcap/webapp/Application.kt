package org.radarbase.redcap.webapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.redcap.listener.HttpClientFactory
import org.radarbase.redcap.managementportal.MpClient
import org.radarbase.redcap.webapp.filter.CorsFilter
import javax.inject.Singleton
import javax.ws.rs.ext.ContextResolver

/**
 * Application configuration.
 *
 */
internal class Application : ResourceConfig() {
    init {
        packages(
            "org.radarbase.redcap.webapp.resource",
            "org.radarbase.redcap.managementportal",
            "org.radarbase.redcap.webapp.exception"
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
        register(ContextResolver {
            ObjectMapper().registerModule(KotlinModule())
        })
    }
}