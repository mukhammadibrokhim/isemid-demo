package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;

import java.util.List;

@Schema(description = "Статистика по эпидемиологическим картам расследования в рамках доступа текущей "
        + "организации.")
public record CardStatsResponse(
        @Schema(description = "Общее количество карт за всё время (без ограничения по дате) — см. верхнеуровневое "
                + "поле generatedAt для момента расчёта.")
        long total,

        @Schema(description = "Количество карт, которые ещё не утверждены супервайзером (все статусы, "
                + "кроме APPROVED), за всё время.")
        long active,

        @Schema(description = "Разбивка по статусу карты, за всё время.")
        List<CardStatusCountResponse> byStatus,

        @Schema(description = "Разбивка по типу карты, за всё время.")
        List<CardTypeCountResponse> byType,

        @Schema(description = "Динамика назначения карт по месяцам — тот же период, что и в dynamics верхнего уровня.")
        TimeSeriesResponse dynamics
) {
}
