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

        addBearerSecurity(operation);

        if (routePolicyResolver.isOrganizationHeaderRequired(path)) {
            addOrganizationHeader(operation);
        }
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
}