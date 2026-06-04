package uz.uzinfocom.app.platform.web.openapi;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.server.PathContainer;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import uz.uzinfocom.app.platform.security.context.SecurityHeaders;
import uz.uzinfocom.app.platform.security.whitelist.SecurityRouteCatalog;
import uz.uzinfocom.app.shared.response.ErrorResponse;
import uz.uzinfocom.app.shared.response.FieldViolationResponse;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    private static final String APPLICATION_JSON = "application/json";
    private static final String ERROR_RESPONSE_SCHEMA_REF = "#/components/schemas/ErrorResponse";

    private static final String ORGANIZATION_HEADER_DESCRIPTION =
            "Идентификатор выбранной организации пользователя. Используется для определения прав доступа в рамках организации.";

    private static final String BAD_REQUEST_DESCRIPTION = "Некорректный запрос.";
    private static final String UNAUTHORIZED_DESCRIPTION = "Требуется аутентификация.";
    private static final String FORBIDDEN_DESCRIPTION = "Доступ запрещён.";
    private static final String NOT_FOUND_DESCRIPTION = "Запрошенный ресурс не найден.";
    private static final String CONFLICT_DESCRIPTION = "Обнаружен конфликт данных.";
    private static final String INTERNAL_ERROR_DESCRIPTION = "Внутренняя ошибка сервера.";

    private final PathPatternParser parser = new PathPatternParser();
    private final Map<String, PathPattern> patternCache = new ConcurrentHashMap<>();

    @Bean
    public OpenAPI isemidOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ISEMID Platform Foundation API")
                        .description("REST API платформы ISEMID для работы с пользователями, организациями, ролями, правами доступа и основными бизнес-сущностями.")
                        .version("1.0.0"))
                .components(openApiComponents())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public GroupedOpenApi referencesOpenApi(
            OpenApiCustomizer commonOpenApiCustomizer,
            OperationCustomizer securityAndOrganizationHeaderCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("Справочники")
                .displayName("Справочники")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Справочники")
                        .description("API для работы со справочниками, классификаторами и нормативно-справочной информацией.")
                        .version("1.0.0")))
                .addOpenApiCustomizer(commonOpenApiCustomizer)
                .addOperationCustomizer(securityAndOrganizationHeaderCustomizer)
                .pathsToMatch(
                        "/v1/catalog/**",
                        "/v1/catalogs/**",
                        "/v1/value-set/**",
                        "/v1/valueset/**",
                        "/v1/mkb10/**",
                        "/v1/country/**",
                        "/v1/countries/**",
                        "/v1/region/**",
                        "/v1/regions/**",
                        "/v1/district/**",
                        "/v1/districts/**",
                        "/api/v1/references/**",
                        "/v1/organizations/lookup"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi mainOpenApi(
            OpenApiCustomizer commonOpenApiCustomizer,
            OperationCustomizer securityAndOrganizationHeaderCustomizer
    ) {
        return GroupedOpenApi.builder()
                .group("Основные API")
                .displayName("Основные API")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Основные API")
                        .description("Основные бизнес-API системы.")
                        .version("1.0.0")))
                .addOpenApiCustomizer(commonOpenApiCustomizer)
                .addOperationCustomizer(securityAndOrganizationHeaderCustomizer)
                .pathsToMatch("/v1/**")
                .pathsToExclude(
                        "/v1/catalog/**",
                        "/v1/catalogs/**",
                        "/v1/value-set/**",
                        "/v1/valueset/**",
                        "/v1/mkb10/**",
                        "/v1/country/**",
                        "/v1/countries/**",
                        "/v1/region/**",
                        "/v1/regions/**",
                        "/v1/district/**",
                        "/v1/districts/**",
                        "/api/v1/references/**",
                        "/v1/organizations/lookup"
                )
                .build();
    }

    @Bean
    public OpenApiCustomizer commonOpenApiCustomizer() {
        return openApi -> {
            registerCommonSchemas(openApi);
            addCommonErrorResponsesToAllOperations(openApi);
        };
    }

    @Bean
    public OperationCustomizer securityAndOrganizationHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            String path = resolvePath(handlerMethod);

            if (isPublicRoute(path)) {
                operation.setSecurity(List.of());
                return operation;
            }

            addBearerSecurity(operation);

            if (isOrganizationHeaderRequired(path)) {
                addOrganizationHeader(operation);
            }

            return operation;
        };
    }

    private Components openApiComponents() {
        Components components = new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .name(BEARER_AUTH)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        registerCommonSchemas(components);

        return components;
    }

    private void registerCommonSchemas(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }

        registerCommonSchemas(openApi.getComponents());
    }

    private void registerCommonSchemas(Components components) {
        if (components.getSchemas() == null) {
            components.setSchemas(new LinkedHashMap<>());
        }

        registerSchema(components, ErrorResponse.class);
        registerSchema(components, FieldViolationResponse.class);
    }

    @SuppressWarnings("unchecked")
    private void registerSchema(Components components, Class<?> schemaClass) {
        Map<String, Schema<?>> schemas =
                (Map<String, Schema<?>>) (Map<?, ?>) ModelConverters.getInstance()
                        .readAll(schemaClass);

        schemas.forEach((schemaName, schema) -> {
            if (!components.getSchemas().containsKey(schemaName)) {
                components.addSchemas(schemaName, schema);
            }
        });
    }

    private void addCommonErrorResponsesToAllOperations(OpenAPI openApi) {
        Paths paths = openApi.getPaths();

        if (paths == null || paths.isEmpty()) {
            return;
        }

        paths.values().forEach(pathItem ->
                pathItem.readOperations().forEach(this::addCommonErrorResponses)
        );
    }

    private void addCommonErrorResponses(Operation operation) {
        ApiResponses responses = operation.getResponses();

        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        addIfAbsent(responses, "400", BAD_REQUEST_DESCRIPTION);
        addIfAbsent(responses, "401", UNAUTHORIZED_DESCRIPTION);
        addIfAbsent(responses, "403", FORBIDDEN_DESCRIPTION);
        addIfAbsent(responses, "404", NOT_FOUND_DESCRIPTION);
        addIfAbsent(responses, "409", CONFLICT_DESCRIPTION);
        addIfAbsent(responses, "500", INTERNAL_ERROR_DESCRIPTION);
    }

    private void addIfAbsent(ApiResponses responses, String status, String description) {
        if (responses.containsKey(status)) {
            return;
        }

        responses.addApiResponse(
                status,
                new ApiResponse()
                        .description(description)
                        .content(errorResponseContent())
        );
    }

    private Content errorResponseContent() {
        return new Content().addMediaType(
                APPLICATION_JSON,
                new MediaType().schema(new Schema<>().$ref(ERROR_RESPONSE_SCHEMA_REF))
        );
    }

    private void addBearerSecurity(Operation operation) {
        List<SecurityRequirement> security = operation.getSecurity();

        if (security != null && security.stream().anyMatch(this::hasBearerAuth)) {
            return;
        }

        operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    private boolean hasBearerAuth(SecurityRequirement requirement) {
        return requirement != null && requirement.containsKey(BEARER_AUTH);
    }

    private void addOrganizationHeader(Operation operation) {
        List<Parameter> parameters = operation.getParameters();

        if (parameters != null && parameters.stream().anyMatch(this::isOrganizationHeader)) {
            return;
        }

        operation.addParametersItem(new Parameter()
                .in("header")
                .name(SecurityHeaders.ORGANIZATION_ID)
                .required(true)
                .description(ORGANIZATION_HEADER_DESCRIPTION)
                .schema(new StringSchema().format("uuid"))
                .example("11111111-1111-1111-1111-111111111111"));
    }

    private boolean isOrganizationHeader(Parameter parameter) {
        return parameter != null
                && "header".equals(parameter.getIn())
                && SecurityHeaders.ORGANIZATION_ID.equalsIgnoreCase(parameter.getName());
    }

    private boolean isPublicRoute(String path) {
        return SecurityRouteCatalog.OPEN_PATTERNS.stream()
                .anyMatch(pattern -> matches(pattern, path));
    }

    private boolean isOrganizationHeaderRequired(String path) {
        Optional<SecurityRouteCatalog.RoutePolicyRule> explicitRule = SecurityRouteCatalog.POLICY_RULES.entrySet()
                .stream()
                .filter(entry -> matches(entry.getKey(), path))
                .map(Map.Entry::getValue)
                .findFirst();

        if (explicitRule.isPresent()) {
            return explicitRule.get().organizationHeaderRequired();
        }

        boolean noOrgRoute = SecurityRouteCatalog.NO_ORG_HEADER_PATTERNS.stream()
                .anyMatch(pattern -> matches(pattern, path));

        return !noOrgRoute;
    }

    private boolean matches(String pattern, String path) {
        PathPattern compiled = patternCache.computeIfAbsent(pattern, parser::parse);
        return compiled.matches(PathContainer.parsePath(path));
    }

    private String resolvePath(HandlerMethod handlerMethod) {
        String classPath = firstPath(AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(),
                RequestMapping.class
        ));

        String methodPath = firstMethodPath(handlerMethod.getMethod());

        return normalizePath(classPath, methodPath);
    }

    private String firstMethodPath(Method method) {
        return Stream.of(
                        firstPath(AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class)),
                        firstPath(AnnotatedElementUtils.findMergedAnnotation(method, GetMapping.class)),
                        firstPath(AnnotatedElementUtils.findMergedAnnotation(method, PostMapping.class)),
                        firstPath(AnnotatedElementUtils.findMergedAnnotation(method, PutMapping.class)),
                        firstPath(AnnotatedElementUtils.findMergedAnnotation(method, PatchMapping.class)),
                        firstPath(AnnotatedElementUtils.findMergedAnnotation(method, DeleteMapping.class))
                )
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    private String firstPath(RequestMapping mapping) {
        if (mapping == null) {
            return null;
        }

        return first(mapping.path(), mapping.value());
    }

    private String firstPath(GetMapping mapping) {
        return mapping == null ? null : first(mapping.path(), mapping.value());
    }

    private String firstPath(PostMapping mapping) {
        return mapping == null ? null : first(mapping.path(), mapping.value());
    }

    private String firstPath(PutMapping mapping) {
        return mapping == null ? null : first(mapping.path(), mapping.value());
    }

    private String firstPath(PatchMapping mapping) {
        return mapping == null ? null : first(mapping.path(), mapping.value());
    }

    private String firstPath(DeleteMapping mapping) {
        return mapping == null ? null : first(mapping.path(), mapping.value());
    }

    private String first(String[] path, String[] value) {
        if (path != null && path.length > 0) {
            return path[0];
        }

        if (value != null && value.length > 0) {
            return value[0];
        }

        return "";
    }

    private String normalizePath(String classPath, String methodPath) {
        String base = classPath == null ? "" : classPath;
        String method = methodPath == null ? "" : methodPath;

        String path = ("/" + base + "/" + method)
                .replaceAll("/+", "/");

        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }
}
