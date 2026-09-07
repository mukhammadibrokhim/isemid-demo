package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

/**
 * One aggregate row per (organization id, {@code patient.category_code},
 * {@link Form129Status}) triple — the raw material for the geography
 * drill-down's form 129 block, grouped by {@code sender_organization_id}.
 */
public record StatisticsOrganizationForm129CountProjection(
        long organizationId,
        String categoryCode,
        Form129Status status,
        long total,
        long female,
        long male,
        long under18,
        long adult
) {
}
