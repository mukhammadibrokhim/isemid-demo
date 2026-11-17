package uz.uzinfocom.app.modules.report.form282.application.query.dto;

/**
 * "Form 28.2" count aggregate — the shape {@code C} this report plugs into
 * {@code ReportHierarchyService} / {@code ReportCountSource}. Carries the
 * confirmed ({@code status = 'APPROVED'}, {@code deleted = false}) {@code
 * form058} / {@code form058_1} case count for one nosological form (or one
 * geography node) whose confirmed final diagnosis ({@code final_icd10_code}, no
 * fallback to the initial {@code icd10_code}) is in the nosological form's code
 * set, split into the columns of the «Kasalxona ichki infeksiyalari» reference
 * form:
 * <ul>
 *   <li>{@code total} — «Qayd qilingan kasalliklar, jami»;</li>
 *   <li>{@code under18} — «17 yoshgacha bo'lgan bolalarda (17 yoshni qo'shgan
 *   holda)» — age &lt; 18 complete years;</li>
 *   <li>{@code underOneMonth} — «1 oygacha» — age &lt; 1 complete month;</li>
 *   <li>{@code oneMonthToUnderOneYear} — «1 oy 1 yoshgacha» — age &gt;= 1 month
 *   and &lt; 1 year.</li>
 * </ul>
 * Age is measured at the case's own {@code created_at}.
 */
public record Form282Counts(long total, long under18, long underOneMonth, long oneMonthToUnderOneYear) {

    public static final Form282Counts EMPTY = new Form282Counts(0, 0, 0, 0);

    /** Component-wise add — used both for the hierarchy engine's {@code merge} and the code roll-up. */
    public Form282Counts plus(Form282Counts other) {
        return new Form282Counts(
                this.total + other.total,
                this.under18 + other.under18,
                this.underOneMonth + other.underOneMonth,
                this.oneMonthToUnderOneYear + other.oneMonthToUnderOneYear
        );
    }
}
