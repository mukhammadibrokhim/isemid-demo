package uz.uzinfocom.app.platform.integrationclient.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Эндпоинт интеграционного API, доступный клиенту благодаря выданному scope.")
public record IntegrationClientEndpointResponse(
        @Schema(description = "Scope, разрешающий вызов, например form058:submit.")
        String scope,

        @Schema(description = "HTTP-метод.", example = "POST")
        String method,

        @Schema(description = "Полный путь с уже подставленным sourceKey клиента.",
                example = "/integration/v1/hepatitis/form-058")
        String path
) {
}
