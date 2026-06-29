package uz.uzinfocom.app.platform.web.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

@Component
public class CommonOpenApiCustomizer implements OpenApiCustomizer {

    private final OpenApiSchemaRegistrar schemaRegistrar;
    private final OpenApiErrorResponseEnricher errorResponseEnricher;
    private final OpenApiSecurityEnricher securityEnricher;

    public CommonOpenApiCustomizer(
            OpenApiSchemaRegistrar schemaRegistrar,
            OpenApiErrorResponseEnricher errorResponseEnricher,
            OpenApiSecurityEnricher securityEnricher
    ) {
        this.schemaRegistrar = schemaRegistrar;
        this.errorResponseEnricher = errorResponseEnricher;
        this.securityEnricher = securityEnricher;
    }

    @Override
    public void customise(OpenAPI openApi) {
        schemaRegistrar.registerCommonSchemas(openApi);
        errorResponseEnricher.enrich(openApi);
        securityEnricher.enrich(openApi);
    }
}