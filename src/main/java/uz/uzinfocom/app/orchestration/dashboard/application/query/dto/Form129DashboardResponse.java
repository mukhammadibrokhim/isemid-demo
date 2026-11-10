package uz.uzinfocom.app.orchestration.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129DiseaseTypeCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129StatusCountResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Полная статистика только по форме №129 (лабораторная серология: сифилис, гепатит B, "
        + "бруцеллёз), в рамках доступа текущей организации — для отдельного просмотра/вкладки по этой форме. "
        + "В отличие от формы №058/№058-1, у формы №129 нет карт/актов (чистый реестр) и нет диагноза МКБ-10 "
        + "(вместо ТОП диагнозов — разбивка по типу заболевания, см. byDiseaseType).")
public record Form129DashboardResponse(
        @Schema(description = "Момент расчёта этого снимка (UTC).")
        Instant generatedAt,

        @Schema(description = "Область видимости, применённая к этому ответу.")
        DashboardScopeResponse scope,

        @Schema(description = "Общее количество форм №129 за всё время.")
        long total,

        @Schema(description = "Количество форм, решение по которым ещё не принято (статус SENT).")
        long active,

        @Schema(description = "Количество форм, зарегистрированных именно в день asOfDate.")
        long newCasesToday,

        @Schema(description = "Дата, за которую посчитано newCasesToday — часовой пояс Asia/Tashkent.")
        LocalDate asOfDate,

        @Schema(description = "Динамика по месяцам, с 1 января текущего календарного года по сегодняшний день. "
                + "canceledCount/approvedCount соответствуют статусам CANCELED/ACCEPTED формы №129.")
        TimeSeriesResponse dynamics,

        @Schema(description = "Разбивка по типу заболевания (сифилис/гепатит B/бруцеллёз), за всё время.")
        List<Form129DiseaseTypeCountResponse> byDiseaseType,

        @Schema(description = "Разбивка по источнику поступления, за всё время.")
        List<SourceCountResponse> sourceBreakdown,

        @Schema(description = "Географический разрез за всё время — та же логика (регион/район/организация "
                + "в зависимости от области видимости), что и в /v1/dashboard/home.")
        List<GeoBreakdownItemResponse> geoBreakdown,

        @Schema(description = "Разбивка по статусу (SENT/ACCEPTED/CANCELED), за всё время.")
        List<Form129StatusCountResponse> byStatus
) {
}
