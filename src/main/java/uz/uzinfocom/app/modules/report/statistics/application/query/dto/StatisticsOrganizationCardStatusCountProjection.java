package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;

/**
 * One aggregate row per (organization id, {@link CardStatus}) pair — the raw
 * material for the geography drill-down's card breakdown, grouped by the
 * owning case's {@code sender_organization_id}.
 */
public record StatisticsOrganizationCardStatusCountProjection(long organizationId, CardStatus status, long count) {
}
