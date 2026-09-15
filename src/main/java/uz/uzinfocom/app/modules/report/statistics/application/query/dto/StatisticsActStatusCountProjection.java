package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;

/**
 * One aggregate row per ({@link StatisticsFormType form}, {@link ActStatus},
 * {@link ActType}) triple across a whole organization scope — the report's
 * root node. Summing over types yields the by-status breakdown, over statuses
 * the by-type breakdown, over both the block total.
 */
public record StatisticsActStatusCountProjection(
        StatisticsFormType formType, ActStatus status, ActType type, long count
) {
}
