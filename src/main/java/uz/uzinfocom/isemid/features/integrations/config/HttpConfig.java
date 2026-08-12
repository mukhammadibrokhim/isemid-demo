package uz.uzinfocom.isemid.features.integrations.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class HttpConfig {

  public static final int READ_TIMEOUT = 5_000;
  public static final int CONNECT_TIMEOUT = 5_000;

  @Bean
  public HttpComponentsClientHttpRequestFactory httpRequestFactory() {
    ConnectionConfig connectionConfig = ConnectionConfig.custom()
        .setConnectTimeout(Timeout.ofMilliseconds(CONNECT_TIMEOUT))
        .build();
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultConnectionConfig(connectionConfig);

    RequestConfig requestConfig = RequestConfig.custom()
        .setResponseTimeout(Timeout.ofMilliseconds(READ_TIMEOUT))
        .build();
    CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .setDefaultRequestConfig(requestConfig)
        .build();
    return new HttpComponentsClientHttpRequestFactory(httpClient);
  }

}
