package uz.uzinfocom.app.platform.iam.application.permission.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации списка прав доступа.")
public record PermissionFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(1)
        Integer page,

        @Schema(description = "Количество записей на странице.", example = "20")
        @Min(1)
        @Max(200)
        Integer size,
        @Schema(description = "Поле для сортировки.", example = "subject")
        String sortBy,
        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Фильтр по субъекту права доступа.", example = "users")
        String subject,
        @Schema(description = "Фильтр по описанию на узбекском языке.")
        String descriptionUz,
        @Schema(description = "Фильтр по описанию на русском языке.")
        String descriptionRu,
        @Schema(description = "Фильтр по описанию на узбекском языке кириллицей.")
        String descriptionUzCyril,
        @Schema(description = "Фильтр по описанию на каракалпакском языке.")
        String descriptionKaa,
        @Schema(description = "Фильтр по признаку активности записи.", example = "true")
        Boolean active
) implements PageableRequest {
}
