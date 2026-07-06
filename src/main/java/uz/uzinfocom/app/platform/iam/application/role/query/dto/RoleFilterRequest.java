package uz.uzinfocom.app.platform.iam.application.role.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации списка ролей.")
public record RoleFilterRequest(

        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{role.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице.", example = "20")
        @Min(value = 1, message = "{role.filter.size.min}")
        @Max(value = 200, message = "{role.filter.size.max}")
        Integer size,

        @Schema(description = "Поле для сортировки.", example = "name")
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Фильтр по наименованию роли.", example = "ROLE_ADMIN")
        String name,

        @Schema(description = "Фильтр по описанию на узбекском языке.")
        String descriptionUz,

        @Schema(description = "Фильтр по описанию на узбекском языке кириллицей.")
        String descriptionUzCyril,

        @Schema(description = "Фильтр по описанию на русском языке.")
        String descriptionRu,

        @Schema(description = "Фильтр по описанию на каракалпакском языке.")
        String descriptionKaa,

        @Schema(description = "Фильтр по признаку активности записи.", example = "true")
        Boolean active

) implements PageableRequest {
}
