package uz.uzinfocom.app.integration.api2.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.integration.api2.common.auth.Api2BearerTokenInterceptor;
import uz.uzinfocom.app.integration.api2.common.properties.Api2Properties;

@Configuration
@EnableConfigurationProperties(Api2Properties.class)
public class Api2RestClientConfiguration {

    @Bean(name = "api2RestClient")
    public RestClient api2RestClient(
            RestClient.Builder builder,
            Api2Properties properties,
            Api2BearerTokenInterceptor bearerTokenInterceptor
    ) {
        return builder.clone()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor(bearerTokenInterceptor)
                .build();
    }
}
