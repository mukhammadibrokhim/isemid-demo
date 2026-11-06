package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "A single read-only SQL statement to run in the dev panel.")
public record DevSqlQueryRequest(
        @NotBlank @Size(max = 20_000) String sql,
        @Schema(description = "Max rows to return; clamped to 1..1000, default 1000.") Integer limit
) {
}
