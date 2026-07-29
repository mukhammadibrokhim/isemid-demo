package uz.uzinfocom.app.platform.http;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Proves the actual mutation path: when a resolved value drifts from what's
 * currently applied, {@link DynamicHttpTuningInterceptor} really does push
 * it onto the live {@link PoolingHttpClientConnectionManager} - not just
 * compute a new value and drop it. Uses a real connection manager (a
 * concrete class with real getters) rather than a mock, so the assertion is
 * on genuine post-mutation state.
 */
class DynamicHttpTuningInterceptorTest {

    @Test
    void appliesAChangedMaxConnectionsValueToTheLiveConnectionManager() throws Exception {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(HttpClients.custom()
                        .setConnectionManager(connectionManager)
                        .build());

        int initialMaxTotal = connectionManager.getMaxTotal();

        SystemSettingResolver systemSettingResolver = mock(SystemSettingResolver.class);
        when(systemSettingResolver.resolveLong(anyString(), anyLong()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(systemSettingResolver.resolveLong(eq("http-client.test.max-connections"), anyLong()))
                .thenReturn((long) (initialMaxTotal + 15));

        DynamicHttpTuningInterceptor interceptor = new DynamicHttpTuningInterceptor(
                "test",
                connectionManager,
                requestFactory,
                systemSettingResolver,
                3000L,
                5000L,
                null,
                null
        );

        HttpRequest request = mock(HttpRequest.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(request, new byte[0])).thenReturn(response);

        ClientHttpResponse result = interceptor.intercept(request, new byte[0], execution);

        assertThat(result).isSameAs(response);
        assertThat(connectionManager.getMaxTotal()).isEqualTo(initialMaxTotal + 15);
    }

    @Test
    void leavesTheConnectionManagerUntouchedWhenNothingChanged() throws Exception {
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(HttpClients.custom()
                        .setConnectionManager(connectionManager)
                        .build());

        int initialMaxTotal = connectionManager.getMaxTotal();
        int initialMaxPerRoute = connectionManager.getDefaultMaxPerRoute();

        SystemSettingResolver systemSettingResolver = mock(SystemSettingResolver.class);
        when(systemSettingResolver.resolveLong(anyString(), anyLong()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        DynamicHttpTuningInterceptor interceptor = new DynamicHttpTuningInterceptor(
                "test",
                connectionManager,
                requestFactory,
                systemSettingResolver,
                3000L,
                5000L,
                null,
                null
        );

        HttpRequest request = mock(HttpRequest.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(request, new byte[0])).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        assertThat(connectionManager.getMaxTotal()).isEqualTo(initialMaxTotal);
        assertThat(connectionManager.getDefaultMaxPerRoute()).isEqualTo(initialMaxPerRoute);
    }
}
