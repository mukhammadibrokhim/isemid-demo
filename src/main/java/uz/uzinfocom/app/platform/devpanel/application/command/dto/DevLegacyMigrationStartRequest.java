package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Start request for the legacy (public) -> public2 data migration.")
public record DevLegacyMigrationStartRequest(
        @Schema(description = "Run everything in one transaction and roll it back at the end, so the "
                + "report can be inspected without changing any data. Defaults to true when omitted.")
        Boolean dryRun,
        @Schema(description = "Must be exactly 'yes' for a real (non-dry) run - the run TRUNCATEs the public2 "
                + "business tables.")
        String confirm,
        @Schema(description = "SQL files to run, from the allowed set. Defaults to the full ordered migration "
                + "(00-prep ... 97-backfill). 95-fix-sequences.sql and 96-fix-organization-provider-key.sql "
                + "can be run on their own.")
        List<String> files
) {
}
