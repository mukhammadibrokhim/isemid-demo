package uz.uzinfocom.app.modules.airquality.application.query.dto;

import java.time.LocalDate;

/** One time bucket of one pollutant, all selected stations pooled; values in µg/m³. */
public record AirBucketStat(LocalDate bucketStart, double average, double maximum, long readings) {
}
