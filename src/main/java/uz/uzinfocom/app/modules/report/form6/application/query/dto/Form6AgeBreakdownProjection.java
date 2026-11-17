package uz.uzinfocom.app.modules.report.form6.application.query.dto;

/**
 * Raw age-bucketed primary-notification counts produced by {@code
 * Form6ReportRepository#countAgeBreakdown} — a single aggregate row over an
 * arbitrary organization-id subtree (a node's whole scope, not grouped by
 * organization). Age brackets are complete calendar years as of the case's
 * {@code created_at} (same {@code age()} technique as Form 1's under14/
 * under18) — see the repository for exact bracket boundaries, including the
 * confirmed rule that age 18 exactly falls into {@link #age19to25()}, not
 * {@link #under18()}. Internal to the report's query service; the web layer
 * never sees this shape directly, see {@code Form6AgeGroupRowResponse}.
 */
public record Form6AgeBreakdownProjection(
        long total,
        long newborn,
        long age1to2,
        long age3to5,
        long age6to14,
        long age15to17,
        long under18,
        long age19to25,
        long age26to40,
        long age41to55,
        long age56to70,
        long over70
) {
    public static final Form6AgeBreakdownProjection EMPTY =
            new Form6AgeBreakdownProjection(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
}
