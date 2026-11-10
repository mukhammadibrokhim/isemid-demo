package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "A database dump (pg_dump) taken from the dev panel.")
public record DevDbDumpResponse(
        String id,
        String fileName,
        List<String> schemas,
        @Schema(description = "File size in bytes; grows while RUNNING, final once SUCCEEDED.")
        long sizeBytes,
        Instant createdAt,
        String createdBy,
        @Schema(description = "RUNNING, SUCCEEDED or FAILED.")
        String status,
        @Schema(description = "pg_dump's error output when FAILED, otherwise null.")
        String error
) {
}
