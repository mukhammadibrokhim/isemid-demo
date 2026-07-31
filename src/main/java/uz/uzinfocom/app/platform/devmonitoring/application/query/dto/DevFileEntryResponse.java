package uz.uzinfocom.app.platform.devmonitoring.application.query.dto;

import java.time.Instant;

public record DevFileEntryResponse(
        String name,
        String path,
        boolean directory,
        Long sizeBytes,
        Instant lastModified
) {
}
