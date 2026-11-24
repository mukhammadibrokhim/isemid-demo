package uz.uzinfocom.app.integration.outbound.monitoring.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Normalized monitoring filter. {@code regionCode} switches the report from
 * "one row per region" to "one row per district of that region";
 * {@code diagnosisCode} (final ICD-10 code, brackets and case already
 * stripped) narrows the case counts.
 */
public record MonitoringFilter(LocalDate from, LocalDate to, String regionCode, String diagnosisCode) {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    public Instant fromInclusive() {
        return from.atStartOfDay(APPLICATION_ZONE).toInstant();
    }

    public Instant toExclusive() {
        return to.plusDays(1).atStartOfDay(APPLICATION_ZONE).toInstant();
    }

    public boolean hasRegion() {
        return regionCode != null;
    }

    public boolean hasDiagnosis() {
        return diagnosisCode != null;
    }
}
