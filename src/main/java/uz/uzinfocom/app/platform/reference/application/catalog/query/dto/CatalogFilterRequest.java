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
        @Min(1)
        Integer page,

        @Min(1)
        @Max(200)
        Integer size,

        String sortBy,

        @Schema(allowableValues = {"asc", "desc"})
        String sortDir,

        @NotNull(message = "{reference.catalog.type.required}")
        CatalogType type,

        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Size(max = 255, message = "{reference.name.max_length}")
        String search
) implements PageableRequest {
}
