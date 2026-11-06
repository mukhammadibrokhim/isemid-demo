package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

import java.util.HashMap;
import java.util.Map;

/**
 * All form 129 counts for one geography node / one period. Form 129 has no
 * confirmed/primary split (its lifecycle is {@code SENT} → {@code ACCEPTED}/
 * {@code CANCELED}) and never carries cards or acts, so this is a lighter
 * shape than {@link StatisticsFormBlockCounts}: a plain {@link #total} with an
 * age (18-year) and gender cut, a per-{@link Form129Status} breakdown, and a
 * per-social-category total ({@link #byCategoryCode}, {@code ref_catalog(type
 * = 'CATEGORY')} code → count). Every field is additive.
 */
public record StatisticsForm129Counts(
        long total,
        long female,
        long male,
        long under18,
        long adult,
        Map<Form129Status, Long> byStatus,
        Map<String, Long> byCategoryCode
) {

    public static final StatisticsForm129Counts EMPTY =
            new StatisticsForm129Counts(0, 0, 0, 0, 0, Map.of(), Map.of());

    public long category(String code) {
        return byCategoryCode.getOrDefault(code, 0L);
    }

    public StatisticsForm129Counts plus(StatisticsForm129Counts other) {
        Map<Form129Status, Long> mergedStatus = new HashMap<>(this.byStatus);
        other.byStatus.forEach((status, count) -> mergedStatus.merge(status, count, Long::sum));

        Map<String, Long> mergedCategory = new HashMap<>(this.byCategoryCode);
        other.byCategoryCode.forEach((code, count) -> mergedCategory.merge(code, count, Long::sum));

        return new StatisticsForm129Counts(
                this.total + other.total,
                this.female + other.female,
                this.male + other.male,
                this.under18 + other.under18,
                this.adult + other.adult,
                mergedStatus,
                mergedCategory
        );
    }
}
