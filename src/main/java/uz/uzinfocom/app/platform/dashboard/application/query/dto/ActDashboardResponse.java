package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;

import java.time.Instant;
import java.util.List;

@Schema(description = "Статистика по актам, в рамках доступа текущей организации.")
public record ActDashboardResponse(
        @Schema(description = "Момент расчёта этого снимка (UTC).")
        Instant generatedAt,

        @Schema(description = "Область видимости, применённая к этому ответу.")
        DashboardScopeResponse scope,

        @Schema(description = "Общее количество актов за всё время.")
        long total,

        @Schema(description = "Разбивка по статусу акта, за всё время.")
        List<ActStatusCountResponse> byStatus,

        @Schema(description = "Динамика назначения актов по месяцам, с 1 января текущего календарного года по сегодняшний день.")
        TimeSeriesResponse dynamics
) {
}
