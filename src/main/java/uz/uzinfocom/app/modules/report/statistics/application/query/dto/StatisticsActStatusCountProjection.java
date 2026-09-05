package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;

/** One aggregate row per {@link ActStatus} across a whole organization scope — the report's root node. */
public record StatisticsActStatusCountProjection(ActStatus status, long count) {
}
