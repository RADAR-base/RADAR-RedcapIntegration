package org.radarbase.redcap.listener

import okhttp3.OkHttpClient
import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class HttpClientFactory :
    DisposableSupplier<OkHttpClient> {

    override fun dispose(instance: OkHttpClient) {
        instance.connectionPool().evictAll()
        val executorService = instance.dispatcher().executorService()
        executorService.shutdown()
        try {
            executorService.awaitTermination(3, TimeUnit.MINUTES)
            logger.info("OkHttp ExecutorService closed.")
        } catch (e: InterruptedException) {
            logger.warn("InterruptedException on destroy()", e)
        }
    }

    override fun get(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    companion object {
        private val logger =
            LoggerFactory.getLogger(HttpClientFactory::class.java)
    }
}