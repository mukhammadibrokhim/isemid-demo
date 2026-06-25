package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import java.time.Instant;

public record Form058DeleteDetailResponse(
        Boolean deleted,
        Instant deletedAt,
        Long deletedBy,
        String deleteReason
) {
}