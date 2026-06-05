package uz.uzinfocom.app.platform.reference.application.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

public record CatalogUpdateRequest(
        @NotNull(message = "{reference.catalog.type.required}")
        CatalogType type,

        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Size(max = 50, message = "{reference.parent_code.max_length}")
        String parentCode,

        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUz,

        @Size(max = 255, message = "{reference.name.max_length}")
        String nameUzCyril,

        @Size(max = 255, message = "{reference.name.max_length}")
        String nameRu,

        @Size(max = 255, message = "{reference.name.max_length}")
        String nameKaa,

        @PositiveOrZero(message = "{validation.must_be_positive}")
        Integer sortOrder
) {
}
