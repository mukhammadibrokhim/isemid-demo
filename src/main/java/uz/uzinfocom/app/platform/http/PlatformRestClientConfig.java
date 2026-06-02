package uz.uzinfocom.app.platform.http;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(PlatformRestClientProperties.class)
public class PlatformRestClientConfig {

    private final PlatformRestClientProperties properties;

    @Bean(destroyMethod = "close")
    public CloseableHttpClient platformCloseableHttpClient() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(properties.getConnectTimeout().toMillis()))
                .setSocketTimeout(Timeout.ofMilliseconds(properties.getReadTimeout().toMillis()))
                .setTimeToLive(TimeValue.ofMilliseconds(properties.getConnectionTimeToLive().toMillis()))
                .build();

        var connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setMaxConnTotal(properties.getMaxConnections())
                .setMaxConnPerRoute(properties.getMaxConnectionsPerRoute())
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(properties.getConnectionRequestTimeout().toMillis()))
                .setResponseTimeout(Timeout.ofMilliseconds(properties.getReadTimeout().toMillis()))
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofMilliseconds(properties.getEvictIdleConnectionsAfter().toMillis()))
                .disableCookieManagement()
                .build();
    }

    @Bean
    public ClientHttpRequestFactory platformClientHttpRequestFactory(
            CloseableHttpClient platformCloseableHttpClient
    ) {
        return new HttpComponentsClientHttpRequestFactory(platformCloseableHttpClient);
    }

    @Bean
    public RestClient restClient(
            RestClient.Builder builder,
            ClientHttpRequestFactory platformClientHttpRequestFactory,
            RestClientTracePropagationInterceptor tracePropagationInterceptor,
            RestClientLoggingInterceptor loggingInterceptor
    ) {
        return builder
                .requestFactory(platformClientHttpRequestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptors(interceptors -> {
                    interceptors.add(tracePropagationInterceptor);
                    interceptors.add(loggingInterceptor);
                })
                .build();
    }
}