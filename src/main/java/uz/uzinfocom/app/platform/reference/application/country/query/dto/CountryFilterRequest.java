package uz.uzinfocom.app.platform.reference.application.country.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Country table filter and pagination parameters.")
public record CountryFilterRequest(
        @Min(1)
        Integer page,

        @Min(1)
        @Max(200)
        Integer size,

        String sortBy,

        @Schema(allowableValues = {"asc", "desc"})
        String sortDir,

        @Size(max = 255, message = "{reference.name.max_length}")
        String name,

        @Size(max = 50, message = "{reference.code.max_length}")
        String code
) implements PageableRequest {
}
