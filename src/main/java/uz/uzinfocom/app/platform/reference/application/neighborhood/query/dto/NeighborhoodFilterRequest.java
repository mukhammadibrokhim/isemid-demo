package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Neighborhood table filter and pagination parameters.")
public record NeighborhoodFilterRequest(
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
        String code,

        @Positive(message = "{validation.must_be_positive}")
        Integer soatoId
) implements PageableRequest {
}
