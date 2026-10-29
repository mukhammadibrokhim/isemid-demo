package uz.uzinfocom.app.modules.report.map.application.query.dto;

import java.time.Instant;

/**
 * One raw row out of {@code MapPointRepository} — {@code status} is the raw
 * string column, not yet parsed into {@code FormStatus}, since a native SQL
 * projection has no reason to depend on the form058 domain enum type.
 */
public record MapPointProjection(
        Long id,
        Double latitude,
        Double longitude,
        String address,
        String diagnosisCode,
        String status,
        Instant createdAt
) {
}
