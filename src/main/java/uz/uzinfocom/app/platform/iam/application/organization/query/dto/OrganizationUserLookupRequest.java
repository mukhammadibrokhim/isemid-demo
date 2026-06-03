package uz.uzinfocom.app.platform.iam.application.organization.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Параметры поиска пользователей внутри организации.")
public record OrganizationUserLookupRequest(

        @Schema(description = "Номер страницы, начиная с 1.", example = "1")
        @Min(1)
        Integer page,

        @Schema(description = "Количество записей на странице.", example = "20")
        @Min(1)
        @Max(50)
        Integer size,

        @Schema(description = "Поле для сортировки.", example = "lastName")
        String sortBy,

        @Schema(description = "Направление сортировки.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Строка поиска по ФИО, логину или ННУЗБ пользователя.")
        String search

) implements PageableRequest {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    @Override
    public Integer page() {
        return page == null ? DEFAULT_PAGE : page;
    }

    @Override
    public Integer size() {
        return size == null ? DEFAULT_SIZE : size;
    }

    public String normalizedSearch() {
        if (search == null || search.isBlank()) {
            return "";
        }

        return search.trim().toLowerCase();
    }
}
