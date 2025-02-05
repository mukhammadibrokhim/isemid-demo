package uz.uzinfocom.isemid.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.BASE_DOMAIN;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.BASE_DOMAIN_LOCAL;

@Configuration
public class SwaggerConfig {
    @Bean
    GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("ISEMID")
                .pathsToMatch("/**")

                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ISEMID API")
                        .version("1.0")
                        .description("<h4>ENG: This is the OpenAPI documentation for the </br> <b>(ISEMID) Information system of epidemiological monitoring of infectious diseases system.</b></h4>" +
                                "<h4>RU: Это документация OpenAPI для </br><b>(ИСЕМИЗ) Информационная система эпидемиологического мониторинга инфекционных заболеваний.</b></h4>"))
                .servers(List.of(
                        new Server().url(BASE_DOMAIN).description("ISEMID API"),
                        new Server().url(BASE_DOMAIN_LOCAL).description("LOCAL")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JTW")));
    }
}
