package uz.uzinfocom.isemid.commons.configs.properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class IntegrationRestClientConfig {

    @Bean
    public RestClient integrationRestClient(
            RestClient.Builder builder,
            IntegrationProperties properties
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.http().connectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.http().readTimeout());

        return builder
                .requestFactory(requestFactory)
                .build();
    }
}