package uz.uzinfocom.app.integration.dhp.common.config;

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
import uz.uzinfocom.app.integration.dhp.common.properties.DhpProperties;
import uz.uzinfocom.app.platform.http.RestClientLoggingInterceptor;
import uz.uzinfocom.app.platform.observability.TraceIdClientHttpRequestInterceptor;

/**
 * Dedicated {@code RestClient} for DHP server-to-server calls, built like
 * {@code Api2RestClientConfiguration}: its own pooled Apache HttpClient so
 * timeouts are tuned independently of the other integrations. No
 * {@code baseUrl} - the token endpoint, the egov employment API and the FHIR
 * server live on different hosts, so every call passes an absolute URI.
 */
@Configuration
@EnableConfigurationProperties(DhpProperties.class)
public class DhpRestClientConfiguration {

    @Bean(name = "dhpConnectionManager")
    public PoolingHttpClientConnectionManager dhpConnectionManager(DhpProperties properties) {
        if (properties.connectTimeout().isZero() || properties.connectTimeout().isNegative()
                || properties.readTimeout().isZero() || properties.readTimeout().isNegative()) {
            throw new IllegalStateException("DHP connect and read timeouts must be positive");
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

    @Bean(name = "dhpCloseableHttpClient", destroyMethod = "close")
    public CloseableHttpClient dhpCloseableHttpClient(
            DhpProperties properties,
            @Qualifier("dhpConnectionManager") PoolingHttpClientConnectionManager connectionManager
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

    @Bean(name = "dhpClientHttpRequestFactory")
    public HttpComponentsClientHttpRequestFactory dhpClientHttpRequestFactory(
            @Qualifier("dhpCloseableHttpClient") CloseableHttpClient closeableHttpClient
    ) {
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    @Bean(name = "dhpRestClient")
    public RestClient dhpRestClient(
            RestClient.Builder builder,
            @Qualifier("dhpClientHttpRequestFactory") ClientHttpRequestFactory requestFactory,
            TraceIdClientHttpRequestInterceptor traceIdInterceptor,
            RestClientLoggingInterceptor loggingInterceptor
    ) {
        return builder.clone()
                .requestFactory(requestFactory)
                .requestInterceptors(interceptors -> {
                    interceptors.removeIf(interceptor ->
                            interceptor instanceof TraceIdClientHttpRequestInterceptor
                                    || interceptor instanceof RestClientLoggingInterceptor);
                    interceptors.add(traceIdInterceptor);
                    interceptors.add(loggingInterceptor);
                })
                .build();
    }
}
