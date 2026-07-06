package uz.uzinfocom.app.integration.api2.common.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.integration.api2.common.auth.Api2BearerTokenInterceptor;
import uz.uzinfocom.app.integration.api2.common.properties.Api2Properties;
import uz.uzinfocom.app.platform.http.RestClientLoggingInterceptor;
import uz.uzinfocom.app.platform.observability.TraceIdClientHttpRequestInterceptor;

@Configuration
@EnableConfigurationProperties(Api2Properties.class)
public class Api2RestClientConfiguration {

    @Bean(name = "api2CloseableHttpClient", destroyMethod = "close")
    public CloseableHttpClient api2CloseableHttpClient(Api2Properties properties) {
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

    @Bean(name = "api2ClientHttpRequestFactory")
    public ClientHttpRequestFactory api2ClientHttpRequestFactory(
            @Qualifier("api2CloseableHttpClient") CloseableHttpClient closeableHttpClient
    ) {
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    @Bean(name = "api2RestClient")
    public RestClient api2RestClient(
            RestClient.Builder builder,
            Api2Properties properties,
            @Qualifier("api2ClientHttpRequestFactory") ClientHttpRequestFactory requestFactory,
            TraceIdClientHttpRequestInterceptor traceIdInterceptor,
            RestClientLoggingInterceptor loggingInterceptor,
            Api2BearerTokenInterceptor bearerTokenInterceptor
    ) {
        return builder.clone()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptors(interceptors -> {
                    interceptors.removeIf(interceptor -> interceptor == traceIdInterceptor
                            || interceptor == loggingInterceptor
                            || interceptor == bearerTokenInterceptor);
                    interceptors.add(traceIdInterceptor);
                    interceptors.add(loggingInterceptor);
                    interceptors.add(bearerTokenInterceptor);
                })
                .build();
    }
}
