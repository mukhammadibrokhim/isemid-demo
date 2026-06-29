package uz.uzinfocom.app.platform.web.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String API_VERSION = "1.0.0";
    private static final String BEARER_AUTH = "bearerAuth";

    private final CommonOpenApiCustomizer commonOpenApiCustomizer;
    private final OpenApiSchemaRegistrar schemaRegistrar;

    public OpenApiConfig(
            CommonOpenApiCustomizer commonOpenApiCustomizer,
            OpenApiSchemaRegistrar schemaRegistrar
    ) {
        this.commonOpenApiCustomizer = commonOpenApiCustomizer;
        this.schemaRegistrar = schemaRegistrar;
    }

    @Bean
    public OpenAPI isemidOpenApi() {
        Components components = new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .name(BEARER_AUTH)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        schemaRegistrar.registerCommonSchemas(components);

        return new OpenAPI()
                .info(new Info()
                        .title("ISEMID Platform Foundation API")
                        .description("""
                                REST API платформы ISEMID для работы с пользователями, организациями, \
                                ролями, правами доступа и основными бизнес-сущностями.
                                """)
                        .version(API_VERSION))
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public GroupedOpenApi referencesOpenApi() {
        return buildGroupedOpenApi(OpenApiGroups.REFERENCES);
    }

    @Bean
    public GroupedOpenApi accessControlOpenApi() {
        return buildGroupedOpenApi(OpenApiGroups.ACCESS_CONTROL);
    }

    @Bean
    public GroupedOpenApi mainOpenApi() {
        return GroupedOpenApi.builder()
                .group("main")
                .displayName("Основные API")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Основные API")
                        .description("Основные бизнес-API системы.")
                        .version(API_VERSION)))
                .addOpenApiCustomizer(commonOpenApiCustomizer)
                .pathsToMatch("/v1/**")
                .pathsToExclude(OpenApiGroups.pathsToExcludeFromMain())
                .build();
    }

    private GroupedOpenApi buildGroupedOpenApi(ApiDocumentationGroup group) {
        return GroupedOpenApi.builder()
                .group(group.group())
                .displayName(group.displayName())
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title(group.title())
                        .description(group.description())
                        .version(API_VERSION)))
                .addOpenApiCustomizer(commonOpenApiCustomizer)
                .pathsToMatch(group.pathsToMatch())
                .build();
    }
}