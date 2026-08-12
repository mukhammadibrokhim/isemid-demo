package uz.uzinfocom.isemid.features.integrations.dmed.config;

import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.commons.configs.logging.RestClientLoggingInterceptor;
import uz.uzinfocom.isemid.features.integrations.dmed.DmedClient;
import uz.uzinfocom.isemid.features.integrations.dmed.DmedForm0581Client;

@Configuration
public class DmedClientConfig {

    private static final String DMED_FORM058_CHANGE_PUSH_API_BASE_URL="https://zordoc.uz";
    private static final String DMED_FORM058_CHANGE_PUSH_API_PATH="/api/integrations/v1/058/{formId}";
    private static final String DMED_FORM0581_CHANGE_PUSH_API_PATH="/api/integrations/v1/058-1/{formId}";
    public static final String TN = "Bearer 64597308|oVsp0wrYokxirnyKq3RHaEE3xbzEagC0nPpgEthG1273abf7";


    /**
     *
     * properties should be added as method first
     *                   argument to the client below as :
     * @Bean
     *     public DmedClient dmedClient(IntegrationProperties properties, RestClientLoggingInterceptor loggingInterceptor)
     */

    @Bean
    public DmedClient dmedClient(RestClientLoggingInterceptor loggingInterceptor,
        HttpComponentsClientHttpRequestFactory httpRequestFactory) {
        RestClient restClient = RestClient.builder()
                .requestFactory(httpRequestFactory)
                .defaultHeaders(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION,TN))
                .uriBuilderFactory(new DefaultUriBuilderFactory(UriComponentsBuilder
                    .fromUriString(DMED_FORM058_CHANGE_PUSH_API_BASE_URL)
                    .path(DMED_FORM058_CHANGE_PUSH_API_PATH)))
                .requestInterceptor(loggingInterceptor)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(DmedClient.class);
    }

    @Bean
    public DmedForm0581Client dmedForm0581Client(RestClientLoggingInterceptor loggingInterceptor,
        HttpComponentsClientHttpRequestFactory httpRequestFactory) {
        RestClient restClient = RestClient.builder()
                .requestFactory(httpRequestFactory)
                .defaultHeaders(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION,TN))
                .uriBuilderFactory(new DefaultUriBuilderFactory(UriComponentsBuilder
                    .fromUriString(DMED_FORM058_CHANGE_PUSH_API_BASE_URL)
                    .path(DMED_FORM0581_CHANGE_PUSH_API_PATH)))
                .requestInterceptor(loggingInterceptor)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(DmedForm0581Client.class);
    }
}
