package uz.uzinfocom.app.platform.web.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class OpenApiConfig {

    private static final String API_VERSION = "1.0.0";
    private static final String BEARER_AUTH = "bearerAuth";

    /*
     * springdoc collects every GroupedOpenApi bean into one list and renders
     * the Swagger UI dropdown in that list's order. Spring does not
     * guarantee @Bean declaration order for that collection, so without an
     * explicit @Order the dropdown's actual order is whatever the
     * classpath/reflection happens to produce - which put "Admin" first,
     * the wrong first impression for most API consumers. These constants
     * pin the order explicitly: main first, admin last.
     */
    private static final int ORDER_MAIN = 0;
    private static final int ORDER_REFERENCES = 1;
    private static final int ORDER_ACCESS_CONTROL = 2;
    private static final int ORDER_INTEGRATION = 3;
    private static final int ORDER_ADMIN = 4;

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
    @Order(ORDER_REFERENCES)
    public GroupedOpenApi referencesOpenApi() {
        return buildGroupedOpenApi(OpenApiGroups.REFERENCES);
    }

    @Bean
    @Order(ORDER_ACCESS_CONTROL)
    public GroupedOpenApi accessControlOpenApi() {
        return buildGroupedOpenApi(OpenApiGroups.ACCESS_CONTROL);
    }

    @Bean
    @Order(ORDER_ADMIN)
    public GroupedOpenApi adminOpenApi() {
        return buildGroupedOpenApi(OpenApiGroups.ADMIN);
    }

    @Bean
    @Order(ORDER_INTEGRATION)
    public GroupedOpenApi integrationOpenApi() {
        return buildGroupedOpenApi(OpenApiGroups.INTEGRATION);
    }

    @Bean
    @Order(ORDER_MAIN)
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