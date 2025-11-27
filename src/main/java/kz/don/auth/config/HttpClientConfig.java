package kz.don.auth.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient httpClient(ConfluenceProperties properties) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(50);
        connectionManager.setDefaultMaxPerRoute(20);

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(
                        org.apache.hc.client5.http.config.RequestConfig.custom()
                                .setConnectionRequestTimeout(Timeout.ofMilliseconds(properties.getConnectionTimeout()))
                                .setResponseTimeout(Timeout.ofMilliseconds(properties.getRequestTimeout()))
                                .build()
                )
                .build();
    }
}
