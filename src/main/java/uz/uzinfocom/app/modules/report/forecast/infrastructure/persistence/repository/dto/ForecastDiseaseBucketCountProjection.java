package uz.uzinfocom.app.modules.report.forecast.infrastructure.persistence.repository.dto;

import java.time.LocalDate;

/**
 * One row of the per-disease aggregated forecast series: an ICD-10 code
 * (current best known — final code if present, else initial), the start date
 * of a time bucket, and the number of notifications carrying that code that
 * fell in it. Only non-empty {@code (code, bucket)} pairs are returned — the
 * query service fills the gaps with zeros per code.
 */
public record ForecastDiseaseBucketCountProjection(String diagnosisCode, LocalDate bucketStart, long count) {
}
