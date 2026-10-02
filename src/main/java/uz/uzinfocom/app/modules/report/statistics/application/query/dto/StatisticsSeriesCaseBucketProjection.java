package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import java.time.LocalDate;

/**
 * One time-bucket aggregate row for the "Statistika" dynamics series — a
 * ({@link StatisticsFormType form}, bucket start) pair with its confirmed
 * ({@code status = 'APPROVED'}) and primary ({@code status NOT IN ('APPROVED',
 * 'CANCELED')}) case counts over {@code form058} / {@code form058_1}. Only
 * non-empty buckets come back; the query service maps them onto the fixed
 * bucket axis, zero-filling the gaps.
 */
public record StatisticsSeriesCaseBucketProjection(
        StatisticsFormType formType, LocalDate bucketStart, long confirmed, long primary
) {
}
