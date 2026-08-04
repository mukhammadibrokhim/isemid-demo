package uz.uzinfocom.app.platform.integrationclient.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации списка интеграционных клиентов.")
public record IntegrationClientFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{integration-client.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице.", example = "20")
        @Min(value = 1, message = "{integration-client.filter.size.min}")
        @Max(value = 200, message = "{integration-client.filter.size.max}")
        Integer size,

        @Schema(description = "Поле для сортировки.", example = "id")
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "desc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Фильтр по названию клиента.")
        String name,

        @Schema(description = "Фильтр по clientId.")
        String clientId,

        @Schema(description = "Фильтр по идентификатору организации.")
        Long organizationId,

        @Schema(description = "Фильтр по типу аутентификации.")
        IntegrationAuthType authType,

        @Schema(description = "Фильтр по признаку активности клиента.")
        Boolean active
) implements PageableRequest {
}
