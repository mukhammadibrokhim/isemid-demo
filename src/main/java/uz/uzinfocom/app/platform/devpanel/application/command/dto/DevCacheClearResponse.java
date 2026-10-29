package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Outcome of a cache clear request.")
public record DevCacheClearResponse(
        @Schema(description = "Names of the caches that were cleared.")
        List<String> cleared,
        @Schema(description = "Names of the caches left untouched because they are protected.")
        List<String> skipped,
        @Schema(description = "Total number of entries removed across the cleared caches.")
        long removedEntries
) {
}
