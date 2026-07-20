package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Полная статистика только по форме №058-1 (без формы №058), в рамках доступа текущей "
        + "организации — для отдельного просмотра/вкладки по этой форме. Обзорные, объединённые показатели "
        + "(формы №058 + №058-1 вместе) см. в /v1/dashboard/home.")
public record Form0581DashboardResponse(
        @Schema(description = "Момент расчёта этого снимка (UTC).")
        Instant generatedAt,

        @Schema(description = "Область видимости, применённая к этому ответу.")
        DashboardScopeResponse scope,

        @Schema(description = "Общее количество случаев по форме №058-1 за всё время.")
        long total,

        @Schema(description = "Количество случаев, решение по которым ещё не принято.")
        long active,

        @Schema(description = "Количество случаев, зарегистрированных именно в день asOfDate.")
        long newCasesToday,

        @Schema(description = "Дата, за которую посчитано newCasesToday — часовой пояс Asia/Tashkent.")
        LocalDate asOfDate,

        @Schema(description = "Динамика по месяцам, с 1 января текущего календарного года по сегодняшний день.")
        TimeSeriesResponse dynamics,

        @Schema(description = "ТОП-5 диагнозов (МКБ-10) по количеству случаев, за всё время.")
        List<TopDiagnosisResponse> topDiagnoses,

        @Schema(description = "Разбивка по источнику поступления, за всё время.")
        List<SourceCountResponse> sourceBreakdown,

        @Schema(description = "Географический разрез за всё время — та же логика (регион/район/организация "
                + "в зависимости от области видимости), что и в /v1/dashboard/home.")
        List<GeoBreakdownItemResponse> geoBreakdown
) {
}
