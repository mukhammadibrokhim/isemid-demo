package uz.uzinfocom.app.modules.iam.application.organization.query.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.modules.iam.domain.enums.ServiceType;

import java.util.List;
import java.util.Objects;

@Schema(description = "Параметры поиска организаций для справочного выбора.")
public record OrganizationLookupRequest(

        @Schema(description = "Строка поиска по наименованию или реквизитам организации.")
        String search,

        @Schema(description = "Внутренний идентификатор организации.", example = "336")
        Long id,

        @Schema(description = "Уровень организации.")
        OrganizationLevel levelType,

        @Schema(description = "Типы медицинской организации.")
        List<MedicalType> medicalTypes,

        @Schema(description = "Виды услуг организации.")
        List<ServiceType> serviceTypes,

        @Schema(description = "Фильтр по признаку активности записи.", example = "true")
        Boolean active,

        @Schema(description = "Код региона.")
        String regionCode,

        @Schema(description = "Код района.")
        String districtCode,

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

    public List<MedicalType> normalizedMedicalTypes() {
        return normalizedList(medicalTypes);
    }

    public List<ServiceType> normalizedServiceTypes() {
        return normalizedList(serviceTypes);
    }

    private static <T> List<T> normalizedList(List<T> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
