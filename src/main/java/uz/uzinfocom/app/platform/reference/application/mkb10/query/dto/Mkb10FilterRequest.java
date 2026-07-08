package uz.uzinfocom.app.platform.reference.application.mkb10.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "MKB-10 classifier table filter and pagination parameters.")
public record Mkb10FilterRequest(
        @Schema(description = "Page number, starting from 1.", example = "1")
        @Min(value = 1, message = "{reference.mkb10.filter.page.min}")
        Integer page,

        @Schema(description = "Number of records per page. Maximum value is 200.", example = "20")
        @Min(value = 1, message = "{reference.mkb10.filter.size.min}")
        @Max(value = 200, message = "{reference.mkb10.filter.size.max}")
        Integer size,

        @Schema(
                description = "Sort field. Unsupported values fall back to the default sort.",
                example = "code",
                allowableValues = {
                        "id",
                        "code",
                        "level",
                        "nameUz",
                        "nameUzCyril",
                        "nameRu",
                        "nameKaa",
                        "createdAt",
                        "updatedAt"
                }
        )
        String sortBy,

        @Schema(description = "Sort direction.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(description = "Search text matched against MKB-10 names in supported languages.", example = "Tuberkulyoz")
        @Size(max = 255, message = "{reference.name.max_length}")
        String name,

        @Schema(description = "Exact MKB-10 code filter.", example = "A15")
        @Size(max = 20, message = "{reference.mkb10.code.max_length}")
        String code,

        @Schema(description = "Filter by direct parent's external id.", example = "12")
        @Positive(message = "{reference.mkb10.parent_id.positive}")
        Long parentId,

        @Schema(description = "Filter by hierarchy depth.", example = "3")
        Integer level
) implements PageableRequest {
}
