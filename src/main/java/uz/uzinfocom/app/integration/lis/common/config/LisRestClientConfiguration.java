package uz.uzinfocom.app.integration.lis.common.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.integration.lis.common.properties.LisProperties;
import uz.uzinfocom.app.platform.http.RestClientLoggingInterceptor;
import uz.uzinfocom.app.platform.observability.TraceIdClientHttpRequestInterceptor;

/**
 * Dedicated {@code RestClient} for LIS, built the same way
 * {@code Api2RestClientConfiguration} builds API2's: its own pooled Apache
 * HttpClient so LIS timeouts can be tuned independently, and its own named
 * beans so nothing else in the app can accidentally inherit LIS's base URL.
 *
 * <p>Unlike API2 there is no bearer-token interceptor here: LIS authenticates
 * us with a static API key on the query string, and the per-request
 * {@code Organization-Id}/{@code Authorization} headers are the <em>calling
 * user's</em>, so they are attached at the call site rather than globally.
 */
@Configuration
@EnableConfigurationProperties(LisProperties.class)
public class LisRestClientConfiguration {

    @Bean(name = "lisCloseableHttpClient", destroyMethod = "close")
    public CloseableHttpClient lisCloseableHttpClient(LisProperties properties) {
        if (properties.connectTimeout().isZero() || properties.connectTimeout().isNegative()
                || properties.readTimeout().isZero() || properties.readTimeout().isNegative()) {
            throw new IllegalStateException("LIS connect and read timeouts must be positive");
        }

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(properties.connectTimeout().toMillis()))
                .setSocketTimeout(Timeout.ofMilliseconds(properties.readTimeout().toMillis()))
                .build();

        var connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(properties.readTimeout().toMillis()))
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableCookieManagement()
                .build();
    }

    @Bean(name = "lisClientHttpRequestFactory")
    public ClientHttpRequestFactory lisClientHttpRequestFactory(
            @Qualifier("lisCloseableHttpClient") CloseableHttpClient closeableHttpClient
    ) {
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    @Bean(name = "lisRestClient")
    public RestClient lisRestClient(
            RestClient.Builder builder,
            @Qualifier("lisClientHttpRequestFactory") ClientHttpRequestFactory requestFactory,
            TraceIdClientHttpRequestInterceptor traceIdInterceptor,
            RestClientLoggingInterceptor loggingInterceptor
    ) {
        return builder.clone()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
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
