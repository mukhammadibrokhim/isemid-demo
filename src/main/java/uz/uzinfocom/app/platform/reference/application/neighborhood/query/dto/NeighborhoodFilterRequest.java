package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Neighborhood table filter and pagination parameters.")
public record NeighborhoodFilterRequest(
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
                        "code",
                        "parentCode",
                        "soatoId",
                        "parentSoatoId",
                        "nameUz",
                        "nameUzCyril",
                        "nameRu",
                        "nameKaa",
                        "sortOrder",
                        "createdAt",
                        "updatedAt"
                }
        )
        String sortBy,

        @Schema(description = "Sort direction.", example = "asc", allowableValues = {"asc", "desc"})
        String sortDir,

        @Schema(
                description = "Search text matched against Neighborhood names in supported languages.",
                example = "Dalvarzin"
        )
        @Size(max = 255, message = "{reference.name.max_length}")
        String name,

        @Schema(description = "Exact Neighborhood code filter.", example = "AN-202001")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Exact Neighborhood SOATO identifier filter.", example = "1703202001")
        @Positive(message = "{validation.must_be_positive}")
        Integer soatoId
) implements PageableRequest {
}
