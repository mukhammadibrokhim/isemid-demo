package uz.uzinfocom.isemid.features.integrations.dmed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.commons.configs.logging.RestClientLoggingInterceptor;
import uz.uzinfocom.isemid.commons.configs.properties.IntegrationProperties;
import uz.uzinfocom.isemid.features.integrations.dmed.DmedClient;

@Configuration
public class DmedClientConfig {

    public static final int READ_TIMEOUT = 5_000;
    public static final int CONNECT_TIMEOUT = 5_000;


    @Bean
    public DmedClient dmedClient(IntegrationProperties properties, RestClientLoggingInterceptor loggingInterceptor) {
        SimpleClientHttpRequestFactory httpRequestFactory =
            new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        httpRequestFactory.setReadTimeout(READ_TIMEOUT);

        RestClient restClient = RestClient.builder()
                .requestFactory(httpRequestFactory)
//                .baseUrl(properties.hosts().dmedForm058ChangePushApiBaseUrl())
                .uriBuilderFactory(new DefaultUriBuilderFactory(UriComponentsBuilder
                    .fromUriString(properties.hosts().dmedForm058ChangePushApiBaseUrl())
                    .path(properties.paths().dmedForm058ChangePushApiPath())))
                .requestInterceptor(loggingInterceptor)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(DmedClient.class);
    }
}
