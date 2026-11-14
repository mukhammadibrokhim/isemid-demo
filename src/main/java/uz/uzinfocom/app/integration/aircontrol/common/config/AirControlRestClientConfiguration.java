package uz.uzinfocom.app.integration.aircontrol.common.config;

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
import uz.uzinfocom.app.integration.aircontrol.common.properties.AirControlProperties;
import uz.uzinfocom.app.platform.http.DynamicHttpTuningInterceptor;
import uz.uzinfocom.app.platform.observability.TraceIdClientHttpRequestInterceptor;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

/**
 * Dedicated {@code RestClient} for AIRCONTROL, built like {@code DhpRestClientConfiguration}.
 * Basic-auth credentials are added per request by {@code AirControlClient}, never
 * by an interceptor here, so no header with a secret can reach a shared logger.
 */
@Configuration
@EnableConfigurationProperties(AirControlProperties.class)
public class AirControlRestClientConfiguration {

    @Bean(name = "aircontrolConnectionManager")
    public PoolingHttpClientConnectionManager aircontrolConnectionManager(AirControlProperties properties) {
        if (properties.connectTimeout().isZero() || properties.connectTimeout().isNegative()
                || properties.readTimeout().isZero() || properties.readTimeout().isNegative()) {
            throw new IllegalStateException("AirControl connect and read timeouts must be positive");
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

    @Bean(name = "aircontrolCloseableHttpClient", destroyMethod = "close")
    public CloseableHttpClient aircontrolCloseableHttpClient(
            AirControlProperties properties,
            @Qualifier("aircontrolConnectionManager") PoolingHttpClientConnectionManager connectionManager
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

    @Bean(name = "aircontrolClientHttpRequestFactory")
    public HttpComponentsClientHttpRequestFactory aircontrolClientHttpRequestFactory(
            @Qualifier("aircontrolCloseableHttpClient") CloseableHttpClient closeableHttpClient
    ) {
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    @Bean(name = "aircontrolHttpTuningInterceptor")
    public DynamicHttpTuningInterceptor aircontrolHttpTuningInterceptor(
            AirControlProperties properties,
            @Qualifier("aircontrolConnectionManager") PoolingHttpClientConnectionManager connectionManager,
            @Qualifier("aircontrolClientHttpRequestFactory") HttpComponentsClientHttpRequestFactory requestFactory,
            SystemSettingResolver systemSettingResolver
    ) {
        return new DynamicHttpTuningInterceptor(
                "aircontrol",
                connectionManager,
                requestFactory,
                systemSettingResolver,
                properties.connectTimeout().toMillis(),
                properties.readTimeout().toMillis(),
                null,
                null
        );
    }

    @Bean(name = "aircontrolRestClient")
    public RestClient aircontrolRestClient(
            RestClient.Builder builder,
            @Qualifier("aircontrolClientHttpRequestFactory") ClientHttpRequestFactory requestFactory,
            TraceIdClientHttpRequestInterceptor traceIdInterceptor,
            @Qualifier("aircontrolHttpTuningInterceptor") DynamicHttpTuningInterceptor httpTuningInterceptor
    ) {
        return builder.clone()
                .requestFactory(requestFactory)
                .requestInterceptors(interceptors -> {
                    interceptors.clear();
                    interceptors.add(httpTuningInterceptor);
                    interceptors.add(traceIdInterceptor);
                })
                .build();
    }
}
