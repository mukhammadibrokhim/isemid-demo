package uz.uzinfocom.app.modules.report.statistics.application.query;

import uz.uzinfocom.app.modules.report.statistics.application.query.dto.StatisticsAgeGroup;

import java.util.Locale;

/**
 * The optional case-level cross-filters for "Statistika" — gender, the 18-year
 * age band, and a social category ({@code ref_catalog(type = 'CATEGORY')}
 * code). All three are independent and AND together; any combination (including
 * none) is valid.
 * <p>
 * They narrow only the <b>case</b> counts (forms №058, №058-1 and №129 — every
 * one joined to {@code patient}), never the folded-in card/act sub-blocks,
 * which carry no gender/age dimension of their own.
 *
 * @param genderCode   {@code patient.gender_code} ({@code MALE} / {@code
 *                      FEMALE}), or {@code null} for no gender filter
 * @param ageGroup     {@link StatisticsAgeGroup}, or {@code null} for no age filter
 * @param categoryCode {@code patient.category_code}, or {@code null} for no
 *                     category filter
 */
public record StatisticsFilter(String genderCode, StatisticsAgeGroup ageGroup, String categoryCode) {

    public static StatisticsFilter of(String genderCode, StatisticsAgeGroup ageGroup, String categoryCode) {
        return new StatisticsFilter(upper(genderCode), ageGroup, trimToNull(categoryCode));
    }

    public boolean isEmpty() {
        return genderCode == null && ageGroup == null && categoryCode == null;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String upper(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }
}
