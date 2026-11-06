package uz.uzinfocom.app.integration.outbound.monitoring.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Body of {@code GET /integration/v1/{source}/monitoring}. The JSON shape is
 * the legacy monitoring contract and must not change: one {@link Item} per
 * location - a region, or a district of the requested region when
 * {@code regionCode} is set (still reported under {@code regionCode}).
 * {@code diseaseType} is keyed by ICD-10 block like the reference data
 * ({@code (A00-A09)}), cases with no resolvable block under
 * {@code UNCLASSIFIED}.
 */
public record MonitoringResponse(LocalDate toDate, List<Item> data) {

    public record Item(
            String regionCode,
            int totalCases,
            int confirmed,
            int unconfirmed,
            int male,
            int female,
            int unknownGender,
            AgeGroupCount ageGroups,
            Map<String, Integer> diseaseType
    ) {
    }

    /** Field names are the legacy JSON keys (they do not match the real age bounds: see the age groups in the SQL). */
    @SuppressWarnings("java:S116")
    public record AgeGroupCount(
            int age_0_3,
            int age_3_6,
            int age_6_10,
            int age_10_17,
            int age_18_25,
            int age_25_45,
            int age_45_65,
            int age_65_plus,
            int age_unknown
    ) {
    }
}
