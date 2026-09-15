package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

/**
 * One aggregate row per (organization id, {@link StatisticsFormType form},
 * {@link CardStatus}, {@link CardType}) — the raw material for the geography
 * drill-down's card breakdown, grouped by the owning case's {@code
 * sender_organization_id}.
 */
public record StatisticsOrganizationCardStatusCountProjection(
        long organizationId, StatisticsFormType formType, CardStatus status, CardType type, long count
) {
}
