package uz.uzinfocom.app.platform.iam.application.organization.query.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

@Schema(description = "Параметры поиска организаций для справочного выбора.")
public record OrganizationLookupRequest(

        @Schema(description = "Строка поиска по наименованию или реквизитам организации.")
        String search,

        @Schema(description = "Внутренний идентификатор организации.", example = "336")
        Long id,

        @Schema(description = "Уровень организации.")
        OrganizationLevel levelType,

        @Schema(description = "Тип медицинской организации.")
        MedicalType medicalType,

        @Schema(description = "Фильтр по признаку активности записи.", example = "true")
        Boolean active,

        @Schema(description = "Максимальное количество записей в ответе.", example = "20")
        @Min(value = 1, message = "{organization.lookup.limit.min}")
        @Max(value = 50, message = "{organization.lookup.limit.max}")
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
