package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;

/**
 * One aggregate row per (organization id, {@link StatisticsFormType form},
 * {@link ActStatus}, {@link ActType}) — the raw material for the geography
 * drill-down's act breakdown, grouped by the owning case's {@code
 * sender_organization_id}.
 */
public record StatisticsOrganizationActStatusCountProjection(
        long organizationId, StatisticsFormType formType, ActStatus status, ActType type, long count
) {
}
