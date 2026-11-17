package uz.uzinfocom.app.platform.web.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.security.context.SecurityHeaders;

import java.util.List;

@Component
public class OpenApiSecurityEnricher {

    private static final String BEARER_AUTH = "bearerAuth";
    private static final String BASIC_AUTH = "basicAuth";

    static final String INTEGRATION_API_KEY_AUTH = "integrationApiKey";
    static final String INTEGRATION_BASIC_AUTH = "integrationBasicAuth";

    private static final String INTEGRATION_ORGANIZATION_HEADER_DESCRIPTION =
            "Организация, от имени которой выполняется запрос. Обязателен для интеграционного клиента, "
                    + "у которого есть организация по умолчанию, и должен совпадать с ней (или входить в "
                    + "разрешённый список). Для клиента EXTERNAL_TOKEN необязателен — организация-отправитель "
                    + "может быть указана в теле (senderOrganizationUuid). Для SSO/DHP-токена — выбранная "
                    + "организация пользователя.";

    private static final String ORGANIZATION_HEADER_DESCRIPTION =
            "Идентификатор выбранной организации пользователя. Используется для определения прав доступа в рамках организации.";

    private final OpenApiRoutePolicyResolver routePolicyResolver;

    public OpenApiSecurityEnricher(OpenApiRoutePolicyResolver routePolicyResolver) {
        this.routePolicyResolver = routePolicyResolver;
    }

    public void enrich(OpenAPI openApi) {
        Paths paths = openApi.getPaths();

        if (paths == null || paths.isEmpty()) {
            return;
        }

        paths.forEach((path, pathItem) -> {
            if (pathItem == null) {
                return;
            }

            pathItem.readOperations().forEach(operation ->
                    customizeSecurity(operation, path)
            );
        });
    }

    private void customizeSecurity(Operation operation, String path) {
        if (routePolicyResolver.isPublicRoute(path)) {
            operation.setSecurity(List.of());
            return;
        }

        if (routePolicyResolver.isDevPanelRoute(path)) {
            addSecurityScheme(operation, BASIC_AUTH);
            return;
        }

        if (routePolicyResolver.isIntegrationRoute(path)) {
            customizeIntegrationSecurity(operation);
            return;
        }

        addSecurityScheme(operation, BEARER_AUTH);

        if (routePolicyResolver.isOrganizationHeaderRequired(path)) {
            addOrganizationHeader(operation);
        }
    }

    /**
     * Each requirement is an alternative (OR). The trailing empty requirement stands for
     * authType=IP_ALLOWLIST clients, which send no credential at all.
     */
    private void customizeIntegrationSecurity(Operation operation) {
        operation.setSecurity(List.of(
                new SecurityRequirement().addList(BEARER_AUTH),
                new SecurityRequirement().addList(INTEGRATION_API_KEY_AUTH),
                new SecurityRequirement().addList(INTEGRATION_BASIC_AUTH),
                new SecurityRequirement()
        ));

        addOrganizationHeader(operation, false, INTEGRATION_ORGANIZATION_HEADER_DESCRIPTION);
    }

    private void addSecurityScheme(Operation operation, String schemeName) {
        List<SecurityRequirement> security = operation.getSecurity();

        if (security != null && security.stream().anyMatch(requirement -> hasScheme(requirement, schemeName))) {
            return;
        }

        operation.addSecurityItem(new SecurityRequirement().addList(schemeName));
    }

    private boolean hasScheme(SecurityRequirement requirement, String schemeName) {
        return requirement != null && requirement.containsKey(schemeName);
    }

    private void addOrganizationHeader(Operation operation) {
        addOrganizationHeader(operation, true, ORGANIZATION_HEADER_DESCRIPTION);
    }

    private void addOrganizationHeader(Operation operation, boolean required, String description) {
        List<Parameter> parameters = operation.getParameters();

        if (parameters != null && parameters.stream().anyMatch(this::isOrganizationHeader)) {
            return;
        }

        operation.addParametersItem(new Parameter()
                .in("header")
                .name(SecurityHeaders.ORGANIZATION_ID)
                .required(required)
                .description(description)
                .schema(new StringSchema().format("uuid"))
                .example("11111111-1111-1111-1111-111111111111"));
    }

    private boolean isOrganizationHeader(Parameter parameter) {
        return parameter != null
                && "header".equals(parameter.getIn())
                && SecurityHeaders.ORGANIZATION_ID.equalsIgnoreCase(parameter.getName());
    }
}