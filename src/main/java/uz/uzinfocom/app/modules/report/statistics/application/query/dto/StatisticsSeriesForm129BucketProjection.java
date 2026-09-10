package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import java.time.LocalDate;

/**
 * One time-bucket aggregate row for the form №129 branch of the "Statistika"
 * dynamics series — a bucket start with its total {@code form_129} notification
 * count (every status, no confirmed/primary split). Only non-empty buckets are
 * returned.
 */
public record StatisticsSeriesForm129BucketProjection(LocalDate bucketStart, long total) {
}
