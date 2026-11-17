package uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.dto;

import java.time.LocalDate;

/**
 * One row of the aggregated forecast series: the start date of a time bucket
 * (already truncated to day / ISO-week-Monday / month-first in the
 * application time zone by the query) and the number of notifications that
 * fell in it. Only non-empty buckets are returned — the query service fills
 * the gaps with zeros.
 */
public record ForecastBucketCountProjection(LocalDate bucketStart, long count) {
}
