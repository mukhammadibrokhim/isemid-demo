package uz.uzinfocom.app.platform.reference.application.manualreport.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.shared.pagination.PageableRequest;

@Schema(description = "Manual Report table filter and pagination parameters.")
public record ManualReportFilterRequest(
        @Schema(description = "Page number, starting from 1.", example = "1")
        @Min(value = 1, message = "{reference.manual_report.filter.page.min}")
        Integer page,

        @Schema(description = "Number of records per page. Maximum value is 200.", example = "20")
        @Min(value = 1, message = "{reference.manual_report.filter.size.min}")
        @Max(value = 200, message = "{reference.manual_report.filter.size.max}")
        Integer size,

        @Schema(
                description = "Sort field. Unsupported values fall back to the default sort.",
                example = "nameUz",
                allowableValues = {
                        "id",
                        "code",
                        "shortName",
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
                description = "Search text matched against Manual Report names in supported languages.",
                example = "Sil"
        )
        @Size(max = 255, message = "{reference.name.max_length}")
        String name,

        @Schema(description = "Exact Manual Report code filter.", example = "TUBERCULOSIS")
        @Size(max = 50, message = "{reference.code.max_length}")
        String code
) implements PageableRequest {
}
