package uz.uzinfocom.app.platform.reference.application.country.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CountryCreateRequest(
        @NotBlank(message = "{reference.code.required}")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

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
