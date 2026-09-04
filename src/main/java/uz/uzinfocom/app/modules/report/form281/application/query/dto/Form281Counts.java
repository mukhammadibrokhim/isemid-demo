package uz.uzinfocom.app.modules.report.form281.application.query.dto;

/**
 * "Form 28.1" count aggregate — the shape {@code C} this report plugs into
 * {@code ReportHierarchyService} / {@code ReportCountSource} for its geography
 * drill-down. Carries the confirmed ({@code status = 'APPROVED'}, {@code deleted
 * = false}) {@code form058} / {@code form058_1} case count for one nosological
 * form (or one geography node) whose confirmed final diagnosis ({@code
 * final_icd10_code}, no fallback to the initial {@code icd10_code}) is in the
 * nosological form's code set, split into the columns of the reference form:
 * <ul>
 *   <li>{@code total} — «Qayd qilingan kasalliklar, jami»;</li>
 *   <li>{@code female} — «Ayollarda» ({@code patient.gender_code = 'FEMALE'});</li>
 *   <li>{@code under18} — «17 yoshgacha (17 yoshni qo'shgan holda)» — age &lt; 18;</li>
 *   <li>{@code under15} — «14 yoshgacha (14 yoshni qo'shgan holda)» — age &lt; 15;</li>
 *   <li>{@code under1} — «Ulardan 1 yoshgacha» — age &lt; 1;</li>
 *   <li>{@code age1to2} — «1-2 yoshgacha (2 yoshni qo'shgan holda)» — age in [1, 2];</li>
 *   <li>{@code age3to5} — «3-5 yoshdagilarda» — age in [3, 5];</li>
 *   <li>{@code rural*} — the same seven cuts restricted to «Qishloq aholisida»
 *   ({@code patient.population_type_code = 'VILLAGE_RESIDENT'}), except the
 *   report does not repeat «Ayollarda» there — so there is no {@code
 *   ruralFemale}.</li>
 * </ul>
 * Age is complete calendar years at the case's own {@code created_at}.
 */
public record Form281Counts(
        long total,
        long female,
        long under18,
        long under15,
        long under1,
        long age1to2,
        long age3to5,
        long ruralTotal,
        long ruralUnder18,
        long ruralUnder15,
        long ruralUnder1,
        long ruralAge1to2,
        long ruralAge3to5
) {

    public static final Form281Counts EMPTY = new Form281Counts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    /** Component-wise add — used both for the hierarchy engine's {@code merge} and the code roll-up. */
    public Form281Counts plus(Form281Counts other) {
        return new Form281Counts(
                this.total + other.total,
                this.female + other.female,
                this.under18 + other.under18,
                this.under15 + other.under15,
                this.under1 + other.under1,
                this.age1to2 + other.age1to2,
                this.age3to5 + other.age3to5,
                this.ruralTotal + other.ruralTotal,
                this.ruralUnder18 + other.ruralUnder18,
                this.ruralUnder15 + other.ruralUnder15,
                this.ruralUnder1 + other.ruralUnder1,
                this.ruralAge1to2 + other.ruralAge1to2,
                this.ruralAge3to5 + other.ruralAge3to5
        );
    }
}
