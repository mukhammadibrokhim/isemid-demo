package com.abdulatif.emis.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers());
    }

    private List<Server> servers() {
        return List.of(
//                new Server()
//                        .url("")
//                        .description("Test Server"),
                new Server()
                        .url("http://127.0.0.1:6002")
                        .description("Local Server")
        );
    }

    private Info apiInfo() {
        return new Info()
                .title("EMIS API")
                .description("API for EMIS")
                .version("1.0")
                .contact(apiContact());
    }

    private Contact apiContact() {
        return new Contact()
                .name("Abdulatif Jalolov")
                .email("abdulatifjalolov6004273@gmail.com")
                .url("https://t.me/abdu_jalolov");
    }

    @Bean
    @Lazy(false)
    public SwaggerUiConfigParameters swaggerUiConfigParameters() {
        SwaggerUiConfigProperties swaggerUiConfigProperties = new SwaggerUiConfigProperties();
        swaggerUiConfigProperties.setDocExpansion("none");
        return new SwaggerUiConfigParameters(swaggerUiConfigProperties);
    }
}
