package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

/**
 * One aggregate row per ({@code patient.category_code}, {@link Form129Status})
 * pair across a whole organization scope — the raw material for the report's
 * form 129 block. Summing over categories yields the by-status breakdown, over
 * statuses the by-category breakdown, over both the block total and the
 * age/gender cuts. {@code categoryCode} is the raw column value (may be {@code
 * null}); the query service keeps only the ones matching a known {@code
 * ref_catalog(type = 'CATEGORY')} code for the per-category breakdown.
 */
public record StatisticsForm129CountProjection(
        String categoryCode,
        Form129Status status,
        long total,
        long female,
        long male,
        long under18,
        long adult
) {
}
