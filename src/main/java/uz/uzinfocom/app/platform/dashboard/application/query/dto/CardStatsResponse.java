package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;

import java.util.List;

@Schema(description = "Статистика по эпидемиологическим картам расследования в рамках доступа текущей "
        + "организации.")
public record CardStatsResponse(
        @Schema(description = "Общее количество карт.")
        long total,

        @Schema(description = "Количество карт, которые ещё не утверждены супервайзером (все статусы, "
                + "кроме APPROVED).")
        long active,

        @Schema(description = "Разбивка по статусу карты.")
        List<CardStatusCountResponse> byStatus,

        @Schema(description = "Разбивка по типу карты.")
        List<CardTypeCountResponse> byType
) {
}
