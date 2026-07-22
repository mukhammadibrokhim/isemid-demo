package uz.uzinfocom.app.integration.api2.legalentity.api;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.legalentity.domain.LegalEntityLookupSource;

@Schema(description = "Результат поиска юридического лица через внешнюю систему API2.")
public record LegalEntityLookupResponse(
        @Schema(description = "Признак успешного выполнения запроса.")
        boolean success,

        @Schema(description = "Локализованное сообщение о результате запроса.")
        String message,

        @Schema(description = "Источник данных, вернувший результат (например, кеш или живой запрос к API2).")
        String source,

        @Schema(description = "HTTP-статус ответа вышестоящей системы API2.")
        int status,

        @Schema(description = "Сырые данные о юридическом лице, полученные от API2.")
        JsonNode data
) {

    public LegalEntityLookupResponse(
            boolean success,
            String message,
            LegalEntityLookupSource source,
            int status,
            JsonNode data
    ) {
        this(success, message, source == null ? null : source.name(), status, data);
    }
}
