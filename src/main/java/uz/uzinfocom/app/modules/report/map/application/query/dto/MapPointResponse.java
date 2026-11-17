package uz.uzinfocom.app.modules.report.map.application.query.dto;

import java.time.Instant;

/**
 * One form058 case point for the "case map" report. {@code confirmed} is
 * {@code status == "APPROVED"}, surfaced directly so the frontend can color
 * markers without re-deriving the form058 status semantics itself. {@code
 * status} stays the raw string (no dependency on form058's own status enum
 * — no other report under {@code modules.report} imports form058 domain
 * classes, they all work off the same string literals the DB stores).
 */
public record MapPointResponse(
        Long id,
        Double latitude,
        Double longitude,
        String address,
        String diagnosisCode,
        boolean confirmed,
        String status,
        Instant createdAt
) {
}
