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
import uz.uzinfocom.isemid.commons.configs.properties.IntegrationProperties;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "Bearer Auth";

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
                        ApiPaths.Mkb10.ROOT + "/**"
                )
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI(IntegrationProperties integrationProperties) {
        return new OpenAPI()
                .info(new Info()
                        .title("ISEMID API / ИСЕМИЗ API")
                        .version("1.0")
                        .description("""
                                <h4>ENG: This is the OpenAPI documentation for the
                                <br><b>(ISEMID) Information System for Epidemiological Monitoring of Infectious Diseases.</b></h4>
                                <h4>RU: Это документация OpenAPI для
                                <br><b>(ИСЕМИЗ) Информационной системы эпидемиологического мониторинга инфекционных заболеваний.</b></h4>
                                """))
                .servers(List.of(
                        new Server()
                                .url(integrationProperties.hosts().isemidPublicApiBaseUrl())
                                .description("ISEMID Test API")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}