package uz.uzinfocom.app.integration.outbound.monitoring.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Body of {@code GET /integration/v1/{source}/monitoring/dynamics}. The JSON
 * shape is the legacy dynamics contract and must not change: one
 * {@link Item} per (period, location); periods with no cases are present with
 * zero counts. {@code diseaseType} is keyed by ICD-10 block like the
 * reference data ({@code (A00-A09)}).
 */
public record MonitoringDynamicsResponse(LocalDate toDate, List<Item> report) {

    public record Item(
            String period,
            String locationCode,
            int totalCases,
            int confirmed,
            int unconfirmed,
            int male,
            int female,
            AgeGroupCount ageGroups,
            Map<String, Integer> diseaseType
    ) {
    }

    /** Field names are the legacy JSON keys. */
    @SuppressWarnings("java:S116")
    public record AgeGroupCount(
            int age_0_3,
            int age_3_6,
            int age_6_10,
            int age_10_17,
            int age_18_25,
            int age_25_45,
            int age_45_65,
            int age_65_plus
    ) {
    }
}
