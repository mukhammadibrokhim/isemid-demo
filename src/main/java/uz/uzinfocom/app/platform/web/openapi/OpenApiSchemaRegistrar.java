package uz.uzinfocom.app.platform.web.openapi;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.shared.dto.response.ErrorResponse;
import uz.uzinfocom.app.shared.dto.response.FieldViolationResponse;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OpenApiSchemaRegistrar {

    public void registerCommonSchemas(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }

        registerCommonSchemas(openApi.getComponents());
    }

    public void registerCommonSchemas(Components components) {
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

        schemas.forEach((schemaName, schema) ->
                components.getSchemas().putIfAbsent(schemaName, schema)
        );
    }
}