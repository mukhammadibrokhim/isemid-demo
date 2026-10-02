package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Ряд динамики «Statistika» для одного узла географии — число случаев по временным "
        + "интервалам за выбранный период. Пустые интервалы присутствуют с нулями. Формы №058/№058-1/№129 "
        + "идут отдельными сериями.")
public record StatisticsSeriesResponse(
        @Schema(description = "Код узла (код региона/района, id организации, либо код области доступа вызывающего).")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Интервал агрегации: DAY / WEEK / MONTH.")
        StatisticsSeriesBucket bucket,

        @Schema(description = "Начало периода ряда (дата первого интервала).")
        LocalDate from,

        @Schema(description = "Конец периода ряда (дата конца последнего интервала).")
        LocalDate to,

        @Schema(description = "Точки ряда, по возрастанию даты.")
        List<StatisticsSeriesPointResponse> points
) {
}
