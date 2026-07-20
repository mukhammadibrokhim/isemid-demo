package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;

import java.time.Instant;
import java.util.List;

@Schema(description = "Статистика по эпидемиологическим картам расследования, в рамках доступа текущей организации.")
public record CardDashboardResponse(
        @Schema(description = "Момент расчёта этого снимка (UTC).")
        Instant generatedAt,

        @Schema(description = "Область видимости, применённая к этому ответу.")
        DashboardScopeResponse scope,

        @Schema(description = "Общее количество карт за всё время.")
        long total,

        @Schema(description = "Количество карт, которые ещё не утверждены супервайзером (все статусы, кроме APPROVED).")
        long active,

        @Schema(description = "Разбивка по статусу карты, за всё время.")
        List<CardStatusCountResponse> byStatus,

        @Schema(description = "Разбивка по типу карты, за всё время.")
        List<CardTypeCountResponse> byType,

        @Schema(description = "Динамика назначения карт по месяцам, с 1 января текущего календарного года по сегодняшний день.")
        TimeSeriesResponse dynamics
) {
}
