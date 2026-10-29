package uz.uzinfocom.app.platform.integrationclient.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Параметры поиска source-ключей активных интеграционных клиентов для выпадающего списка.")
public record IntegrationClientSourceKeyLookupRequest(
        @Schema(description = "Строка поиска по sourceKey (подстрока, без учёта регистра).")
        String search,

        @Schema(description = "Максимальное количество записей в ответе.", example = "20")
        @Min(value = 1, message = "{integration-client.source-keys.limit.min}")
        @Max(value = 50, message = "{integration-client.source-keys.limit.max}")
        Integer limit
) {

    private static final int DEFAULT_LIMIT = 20;

    public int normalizedLimit() {
        return limit == null ? DEFAULT_LIMIT : limit;
    }

    public String normalizedSearch() {
        if (search == null || search.isBlank()) {
            return "";
        }

        return search.trim().toLowerCase();
    }
}
