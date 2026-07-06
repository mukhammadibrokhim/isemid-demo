package uz.uzinfocom.app.platform.iam.application.organization.query.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации списка организаций.")
public record OrganizationFilerRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{organization.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице.", example = "20")
        @Min(value = 1, message = "{organization.filter.size.min}")
        @Max(value = 200, message = "{organization.filter.size.max}")
        Integer size,

        @Schema(description = "Поле для сортировки.", example = "name")
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Фильтр по наименованию организации.")
        String name,
        @Schema(description = "Фильтр по ИНН организации.")
        String tin,
        @Schema(description = "Фильтр по признаку активности записи.", example = "true")
        Boolean active,
        @Schema(description = "Код региона организации.")
        String regionCode,
        @Schema(description = "Код района или города организации.")
        String districtCode,
        @Schema(description = "Уровень организации.")
        OrganizationLevel levelType,
        @Schema(description = "Тип медицинской организации.")
        MedicalType medicalType

) implements PageableRequest {
}
