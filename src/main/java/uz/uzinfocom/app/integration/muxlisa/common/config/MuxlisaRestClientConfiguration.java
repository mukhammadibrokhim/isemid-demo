package uz.uzinfocom.app.integration.muxlisa.common.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.integration.muxlisa.common.properties.MuxlisaProperties;
import uz.uzinfocom.app.platform.observability.TraceIdClientHttpRequestInterceptor;

/**
 * Dedicated {@code RestClient} for Muxlisa, built like {@code DhpRestClientConfiguration}.
 * {@code RestClientLoggingInterceptor} is deliberately NOT attached: it would
 * log request/response bodies, and these bodies are voice recordings of patients.
 */
@Configuration
@EnableConfigurationProperties(MuxlisaProperties.class)
public class MuxlisaRestClientConfiguration {

    @Bean(name = "muxlisaConnectionManager")
    public PoolingHttpClientConnectionManager muxlisaConnectionManager(MuxlisaProperties properties) {
        if (properties.connectTimeout().isZero() || properties.connectTimeout().isNegative()
                || properties.readTimeout().isZero() || properties.readTimeout().isNegative()) {
            throw new IllegalStateException("Muxlisa connect and read timeouts must be positive");
        }

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(properties.connectTimeout().toMillis()))
                .setSocketTimeout(Timeout.ofMilliseconds(properties.readTimeout().toMillis()))
                .build();

        return PoolingHttpClientConnectionManagerBuilder
                .create()
                .setDefaultConnectionConfig(connectionConfig)
                .build();
    }

    @Bean(name = "muxlisaCloseableHttpClient", destroyMethod = "close")
    public CloseableHttpClient muxlisaCloseableHttpClient(
            MuxlisaProperties properties,
            @Qualifier("muxlisaConnectionManager") PoolingHttpClientConnectionManager connectionManager
    ) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(properties.readTimeout().toMillis()))
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableCookieManagement()
                .build();
    }

    @Bean(name = "muxlisaClientHttpRequestFactory")
    public HttpComponentsClientHttpRequestFactory muxlisaClientHttpRequestFactory(
            @Qualifier("muxlisaCloseableHttpClient") CloseableHttpClient closeableHttpClient
    ) {
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    @Bean(name = "muxlisaRestClient")
    public RestClient muxlisaRestClient(
            RestClient.Builder builder,
            @Qualifier("muxlisaClientHttpRequestFactory") ClientHttpRequestFactory requestFactory,
            TraceIdClientHttpRequestInterceptor traceIdInterceptor
    ) {
        return builder.clone()
                .requestFactory(requestFactory)
                .requestInterceptors(interceptors -> {
                    interceptors.clear();
                    interceptors.add(traceIdInterceptor);
                })
                .build();
    }
}
