package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "State of the latest legacy data migration run.")
public record DevLegacyMigrationResponse(
        @Schema(description = "Run id, increments per run since application start.")
        long runId,
        @Schema(description = "RUNNING, SUCCEEDED or FAILED.")
        String status,
        boolean dryRun,
        String startedBy,
        Instant startedAt,
        Instant finishedAt,
        @Schema(description = "Set when status is FAILED.")
        String error,
        List<Step> steps
) {

    @Schema(description = "One SQL file of the run.")
    public record Step(
            String file,
            @Schema(description = "PENDING, RUNNING, OK or FAILED.")
            String status,
            long durationMs,
            String error,
            @Schema(description = "Server warnings/notices raised while the file ran.")
            List<String> notices,
            @Schema(description = "Result sets the file returned (the validation report of 90-finalize, ...).")
            List<Table> tables
    ) {
    }

    public record Table(
            List<String> columns,
            List<List<String>> rows,
            @Schema(description = "True when rows were cut at the per-table cap.")
            boolean truncated
    ) {
    }
}
