package org.radarcns.redcap.listener;

import okhttp3.OkHttpClient;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpClientFactory implements DisposableSupplier<OkHttpClient> {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientFactory.class);

    @Override
    public void dispose(OkHttpClient instance) {
        instance.connectionPool().evictAll();
        ExecutorService executorService = instance.dispatcher().executorService();
        executorService.shutdown();
        try {
            executorService.awaitTermination(3, TimeUnit.MINUTES);
            logger.info("OkHttp ExecutorService closed.");
        } catch (InterruptedException e) {
            logger.warn("InterruptedException on destroy()", e);
        }
    }

    @Override
    public OkHttpClient get() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
