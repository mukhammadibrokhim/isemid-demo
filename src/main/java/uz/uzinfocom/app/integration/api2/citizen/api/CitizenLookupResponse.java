package uz.uzinfocom.app.integration.api2.citizen.api;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;
import uz.uzinfocom.app.integration.api2.citizen.application.mapper.CitizenResponseMapper;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupSource;

@Schema(description = "Результат поиска физического лица через внешнюю систему API2.")
public record CitizenLookupResponse(
        @Schema(description = "Признак успешного выполнения запроса.")
        boolean success,

        @Schema(description = "Локализованное сообщение о результате запроса.")
        String message,

        @Schema(description = "Источник данных, вернувший результат (например, ГЦП, ЗАГС и т.п.).")
        String source,

        @Schema(description = "HTTP-статус ответа вышестоящей системы API2.")
        int status,

        @Schema(description = "Текстовый результат поиска, разобранный из ответа API2.")
        String result,

        @Schema(description = "Дополнительные примечания, разобранные из ответа API2.")
        String comments,

        @Schema(description = "Сырые данные о физическом лице, полученные от API2.")
        JsonNode data
) {

    public CitizenLookupResponse(
            boolean success,
            String message,
            CitizenLookupSource source,
            int status,
            JsonNode payload
    ) {
        this(
                success,
                message,
                source == null ? null : source.name(),
                status,
                CitizenResponseMapper.result(payload),
                CitizenResponseMapper.comments(payload),
                CitizenResponseMapper.data(payload)
        );
    }
}
