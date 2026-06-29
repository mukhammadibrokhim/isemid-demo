package uz.uzinfocom.app.platform.web.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OpenApiErrorResponseEnricher {

    private static final String APPLICATION_JSON = "application/json";
    private static final String ERROR_RESPONSE_SCHEMA_REF = "#/components/schemas/ErrorResponse";

    private static final Map<String, String> COMMON_ERROR_RESPONSES = new LinkedHashMap<>();

    static {
        COMMON_ERROR_RESPONSES.put("400", "Некорректный запрос.");
        COMMON_ERROR_RESPONSES.put("401", "Требуется аутентификация.");
        COMMON_ERROR_RESPONSES.put("403", "Доступ запрещён.");
        COMMON_ERROR_RESPONSES.put("404", "Запрошенный ресурс не найден.");
        COMMON_ERROR_RESPONSES.put("409", "Обнаружен конфликт данных.");
        COMMON_ERROR_RESPONSES.put("500", "Внутренняя ошибка сервера.");
    }

    public void enrich(OpenAPI openApi) {
        Paths paths = openApi.getPaths();

        if (paths == null || paths.isEmpty()) {
            return;
        }

        paths.values().forEach(pathItem -> {
            if (pathItem == null) {
                return;
            }

            pathItem.readOperations().forEach(this::addCommonErrorResponses);
        });
    }

    private void addCommonErrorResponses(Operation operation) {
        ApiResponses responses = operation.getResponses();

        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        ApiResponses finalResponses = responses;
        COMMON_ERROR_RESPONSES.forEach((status, description) ->
                addErrorResponseIfAbsent(finalResponses, status, description)
        );
    }

    private void addErrorResponseIfAbsent(
            ApiResponses responses,
            String status,
            String description
    ) {
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
}