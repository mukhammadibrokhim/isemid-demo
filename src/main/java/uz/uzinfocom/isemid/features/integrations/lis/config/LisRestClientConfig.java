package uz.uzinfocom.isemid.features.integrations.lis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.commons.configs.logging.RestClientLoggingInterceptor;
import uz.uzinfocom.isemid.commons.configs.properties.IntegrationProperties;
import uz.uzinfocom.isemid.features.integrations.lis.LisActClient;

@Configuration
public class LisRestClientConfig {


    @Bean
    public LisActClient lisClient(IntegrationProperties properties,
                                RestClientLoggingInterceptor loggingInterceptor,
                                HttpComponentsClientHttpRequestFactory httpRequestFactory) {

        RestClient restClient = RestClient.builder()
            .requestFactory(httpRequestFactory)
            .uriBuilderFactory(new DefaultUriBuilderFactory(UriComponentsBuilder
                .fromUriString(properties.hosts().lisPublicApiBaseUrl())
                .queryParam("key", properties.keys().lisApiKey())
                .encode()))
            .requestInterceptor(loggingInterceptor)
            .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();
        return factory.createClient(LisActClient.class);
    }
}