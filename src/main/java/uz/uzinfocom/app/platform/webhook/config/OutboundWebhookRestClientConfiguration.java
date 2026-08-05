package uz.uzinfocom.app.platform.webhook.config;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.platform.http.DynamicHttpTuningInterceptor;
import uz.uzinfocom.app.platform.http.RestClientLoggingInterceptor;
import uz.uzinfocom.app.platform.observability.TraceIdClientHttpRequestInterceptor;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

/**
 * Dedicated {@code RestClient} for outbound status-change webhooks, built the
 * same way {@code LisRestClientConfiguration} builds LIS's: its own pooled
 * Apache HttpClient so timeouts can be tuned independently, and its own named
 * beans. Unlike LIS/API2 there is no fixed base URL or shared credential -
 * both the target URL and the auth header are per-{@code IntegrationClient},
 * attached at the call site (see {@code OutboundWebhookClient}).
 */
@Configuration
@EnableConfigurationProperties(OutboundWebhookProperties.class)
public class OutboundWebhookRestClientConfiguration {

    @Bean(name = "outboundWebhookConnectionManager")
    public PoolingHttpClientConnectionManager outboundWebhookConnectionManager(OutboundWebhookProperties properties) {
        if (properties.connectTimeout().isZero() || properties.connectTimeout().isNegative()
                || properties.readTimeout().isZero() || properties.readTimeout().isNegative()) {
            throw new IllegalStateException("Outbound webhook connect and read timeouts must be positive");
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

    @Bean(name = "outboundWebhookCloseableHttpClient", destroyMethod = "close")
    public CloseableHttpClient outboundWebhookCloseableHttpClient(
            OutboundWebhookProperties properties,
            @Qualifier("outboundWebhookConnectionManager") PoolingHttpClientConnectionManager connectionManager
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

    @Bean(name = "outboundWebhookClientHttpRequestFactory")
    public HttpComponentsClientHttpRequestFactory outboundWebhookClientHttpRequestFactory(
            @Qualifier("outboundWebhookCloseableHttpClient") CloseableHttpClient closeableHttpClient
    ) {
        return new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
    }

    @Bean(name = "outboundWebhookHttpTuningInterceptor")
    public DynamicHttpTuningInterceptor outboundWebhookHttpTuningInterceptor(
            OutboundWebhookProperties properties,
            @Qualifier("outboundWebhookConnectionManager") PoolingHttpClientConnectionManager connectionManager,
            @Qualifier("outboundWebhookClientHttpRequestFactory") HttpComponentsClientHttpRequestFactory requestFactory,
            SystemSettingResolver systemSettingResolver
    ) {
        return new DynamicHttpTuningInterceptor(
                "outbound-webhook",
                connectionManager,
                requestFactory,
                systemSettingResolver,
                properties.connectTimeout().toMillis(),
                properties.readTimeout().toMillis(),
                null,
                null
        );
    }

    @Bean(name = "outboundWebhookRestClient")
    public RestClient outboundWebhookRestClient(
            RestClient.Builder builder,
            @Qualifier("outboundWebhookClientHttpRequestFactory") ClientHttpRequestFactory requestFactory,
            TraceIdClientHttpRequestInterceptor traceIdInterceptor,
            RestClientLoggingInterceptor loggingInterceptor,
            @Qualifier("outboundWebhookHttpTuningInterceptor") DynamicHttpTuningInterceptor httpTuningInterceptor
    ) {
        return builder.clone()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptors(interceptors -> {
                    interceptors.removeIf(interceptor ->
                            interceptor instanceof TraceIdClientHttpRequestInterceptor
                                    || interceptor instanceof RestClientLoggingInterceptor
                                    || interceptor instanceof DynamicHttpTuningInterceptor);
                    interceptors.add(httpTuningInterceptor);
                    interceptors.add(traceIdInterceptor);
                    interceptors.add(loggingInterceptor);
                })
                .build();
    }
}
