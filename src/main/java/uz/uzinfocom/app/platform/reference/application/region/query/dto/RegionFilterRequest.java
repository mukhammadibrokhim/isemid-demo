package uz.uzinfocom.app.platform.reference.application.region.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Region table filter and pagination parameters.")
public record RegionFilterRequest(
        @Schema(description = "Page number, starting from 1.", example = "1")
        @Min(value = 1, message = "{reference.region.filter.page.min}")
        Integer page,

        @Schema(description = "Number of records per page. Maximum value is 200.", example = "20")
        @Min(value = 1, message = "{reference.region.filter.size.min}")
        @Max(value = 200, message = "{reference.region.filter.size.max}")
        Integer size,

        @Schema(
                description = "Sort field. Unsupported values fall back to the default sort.",
                example = "nameUz",
                allowableValues = {
                        "id",
                        "code",
                        "parentCode",
                        "soatoId",
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

        @Schema(
                description = "Search text matched against Region names in supported languages.",
                example = "Andijon"
        )
        @Size(max = 255, message = "{reference.name.max_length}")
        String name,

        @Schema(description = "Exact Region code filter.", example = "UZ-AN")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code,

        @Schema(description = "Exact Region SOATO identifier filter.", example = "1703")
        @Positive(message = "{validation.must_be_positive}")
        Integer soatoId
) implements PageableRequest {
}
