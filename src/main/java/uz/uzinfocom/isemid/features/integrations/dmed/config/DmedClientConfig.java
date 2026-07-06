package uz.uzinfocom.isemid.features.integrations.dmed.config;

import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.commons.configs.logging.RestClientLoggingInterceptor;
import uz.uzinfocom.isemid.features.integrations.dmed.DmedClient;

@Configuration
public class DmedClientConfig {

    public static final int READ_TIMEOUT = 5_000;
    public static final int CONNECT_TIMEOUT = 5_000;
    private static final String DMED_FORM058_CHANGE_PUSH_API_BASE_URL="https://bane-backend.mis.2112.uz";
    private static final String DMED_FORM058_CHANGE_PUSH_API_PATH="/api/integrations/v1/058/{formId}";
    public static final String TN = "Bearer 66258|cbMcnaUQfO1fB5zNcH4PMhwlxD4oiXC6lb8ZlJzz9776b081";


    /**
     *
     * properties should be added as method first
     *                   argument to the client below as :
     * @Bean
     *     public DmedClient dmedClient(IntegrationProperties properties, RestClientLoggingInterceptor loggingInterceptor)
     * @param loggingInterceptor
     * @return
     */

    @Bean
    public DmedClient dmedClient(RestClientLoggingInterceptor loggingInterceptor) {
        SimpleClientHttpRequestFactory httpRequestFactory =
            new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        httpRequestFactory.setReadTimeout(READ_TIMEOUT);

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
}
