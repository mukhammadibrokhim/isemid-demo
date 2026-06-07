package uz.uzinfocom.app.platform.reference.application.catalog.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Catalog table filter and pagination parameters.")
public record CatalogFilterRequest(
        @Schema(description = "Page number, starting from 1.", example = "1")
        @Min(1)
        Integer page,

        @Schema(description = "Number of records per page. Maximum value is 200.", example = "20")
        @Min(1)
        @Max(200)
        Integer size,

        @Schema(
                description = "Sort field. Unsupported values fall back to the default sort.",
                example = "sortOrder",
                allowableValues = {
                        "id",
                        "type",
                        "code",
                        "parentCode",
                        "nameUz",
                        "nameRu",
                        "sortOrder"
                }
        )
        String sortBy,

        @Schema(description = "Sort direction.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(
                description = "Catalog type.",
                example = "GENDER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "{reference.catalog.type.required}")
        CatalogType type,

        @Schema(description = "Exact Catalog item code filter.")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Exact parent item code filter inside the same catalog type.")
        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Schema(description = "Search text matched against item code and localized names.")
        @Size(max = 255, message = "{reference.name.max_length}")
        String search
) implements PageableRequest {
}
