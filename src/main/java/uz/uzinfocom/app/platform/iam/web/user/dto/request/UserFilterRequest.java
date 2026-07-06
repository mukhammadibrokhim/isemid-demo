package uz.uzinfocom.app.platform.iam.web.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры фильтрации и пагинации списка пользователей.")
public record UserFilterRequest(
        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(value = 1, message = "{user.filter.page.min}")
        Integer page,

        @Schema(description = "Количество записей на странице.", example = "20")
        @Min(value = 1, message = "{user.filter.size.min}")
        @Max(value = 200, message = "{user.filter.size.max}")
        Integer size,

        @Schema(description = "Поле для сортировки.", example = "lastName")
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Фильтр по имени пользователя.")
        String firstName,
        @Schema(description = "Фильтр по фамилии пользователя.")
        String lastName,
        @Schema(description = "Фильтр по отчеству пользователя.")
        String middleName,
        @Schema(description = "Фильтр по ННУЗБ пользователя.")
        String nnuzb,
        @Schema(description = "Фильтр по номеру телефона пользователя.")
        String phoneNumber,
        @Schema(description = "Фильтр по признаку активности пользователя.", example = "true")
        Boolean active
) implements PageableRequest {
}
