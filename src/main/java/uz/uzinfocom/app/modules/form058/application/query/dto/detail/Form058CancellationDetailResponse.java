package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import java.time.Instant;

public record Form058CancellationDetailResponse(
        String cancelReason,
        Long canceledBy,
        Instant canceledAt,
        String notApprovedReason
) {
}