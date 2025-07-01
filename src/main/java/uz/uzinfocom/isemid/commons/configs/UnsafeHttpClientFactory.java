package uz.uzinfocom.isemid.commons.configs;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

public class UnsafeHttpClientFactory {

    public static RestTemplate create() {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            HttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext))
                    .build();

            CloseableHttpClient client = HttpClientBuilder.create()
                    .setConnectionManager(connManager)
                    .build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);
            return new RestTemplate(factory);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create unsafe RestTemplate", e);
        }
    }
}
