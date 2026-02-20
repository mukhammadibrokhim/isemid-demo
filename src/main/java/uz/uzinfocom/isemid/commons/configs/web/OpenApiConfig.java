package uz.uzinfocom.isemid.commons.configs.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;

import java.util.List;

@Configuration
public class OpenApiConfig {


    @Bean
    public GroupedOpenApi publicClassifierGroup() {
        return GroupedOpenApi.builder()
                .group("📦Справочники")
                .pathsToMatch(
                        ApiPaths.ManualReport.ROOT + "/**",
                        ApiPaths.Catalog.ROOT + "/**",
                        ApiPaths.Mkb10.ROOT + "/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi mainApiGroup() {
        return GroupedOpenApi.builder()
                .group("🌐 Основной API")
                .pathsToMatch(ApiPaths.BASE_PATH + "/**")
                .pathsToExclude(
                        ApiPaths.ManualReport.ROOT + "/**",
                        ApiPaths.Catalog.ROOT + "/**",
                        ApiPaths.Mkb10.ROOT + "/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ISEMID API / ИСЕМИЗ API")
                        .version("1.0")
                        .description("<h4>ENG: This is the OpenAPI documentation for the </br> <b>(ISEMID) Information system of epidemiological monitoring of infectious diseases system.</b></h4>" +
                                "<h4>RU: Это документация OpenAPI для </br><b>(ИСЕМИЗ) Информационная система эпидемиологического мониторинга инфекционных заболеваний.</b></h4>"))
                .servers(List.of(
                        new Server().url(ApiPaths.BaseDomain.ROOT).description("ISEMID API"),
                        new Server().url(ApiPaths.BaseDomain.ROOT_DEV).description("ISEMID DEV API"),
                        new Server().url(ApiPaths.BaseDomain.ROOT_LOCAL).description("LOCAL")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JTW")));
    }
}
