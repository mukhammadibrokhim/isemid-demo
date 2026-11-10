package uz.uzinfocom.app.modules.report.form13.application.query.dto;

/**
 * "Form 13" per-disease count triple for a single geography node and a single
 * period: confirmed ({@code status = 'APPROVED'}) {@code form058} / {@code
 * form058_1} cases whose confirmed final diagnosis ({@code final_icd10_code} —
 * no fallback to the initial {@code icd10_code}) is in one {@code FORM_13}
 * catalog entry's ICD-10 set.
 * <ul>
 *   <li>{@code total} — every matching notification;</li>
 *   <li>{@code under14} — the subset under 14 complete calendar years at {@code created_at};</li>
 *   <li>{@code under18} — the subset under 18 (a superset of {@code under14}).</li>
 * </ul>
 */
public record Form13Metric(long total, long under14, long under18) {

    public static final Form13Metric EMPTY = new Form13Metric(0, 0, 0);

    public Form13Metric plus(Form13Metric other) {
        return new Form13Metric(
                this.total + other.total,
                this.under14 + other.under14,
                this.under18 + other.under18
        );
    }
}
