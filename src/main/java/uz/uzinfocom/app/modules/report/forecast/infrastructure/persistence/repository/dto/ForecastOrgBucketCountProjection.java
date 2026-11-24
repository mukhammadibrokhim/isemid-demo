package uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.dto;

import java.time.LocalDate;

/**
 * One row of the per-organization forecast series: which sending
 * organization, which time bucket, and how many notifications fell in it.
 * Folded into per-geography-node series by {@code
 * ForecastGeographyCountSource} + the shared hierarchy engine.
 */
public record ForecastOrgBucketCountProjection(long organizationId, LocalDate bucketStart, long count) {
}
