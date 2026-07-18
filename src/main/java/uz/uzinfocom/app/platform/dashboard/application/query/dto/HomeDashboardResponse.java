package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Данные главного дашборда: сводка по случаям, динамика, ТОП диагнозов, географический "
        + "разрез, тибиотические учреждения, карты и акты — всё в рамках доступа текущей организации. "
        + "Все агрегаты, не имеющие собственного явного периода (caseSummary без newCasesToday, topDiagnoses, "
        + "geoBreakdown, cardStats/actStats без dynamics), посчитаны за всё время по состоянию на generatedAt.")
public record HomeDashboardResponse(
        @Schema(description = "Момент расчёта этого снимка дашборда (UTC). Момент \"истины\" для всех "
                + "агрегатов за всё время в этом ответе — они актуальны по состоянию именно на этот момент, "
                + "а не на момент получения ответа клиентом.")
        Instant generatedAt,

        @Schema(description = "Область видимости, применённая к этому ответу.")
        DashboardScopeResponse scope,

        @Schema(description = "Сводка по случаям (формы №058 и №058-1).")
        CaseSummaryResponse caseSummary,

        @Schema(description = "Динамика случаев (форма №058 + форма №058-1) по месяцам, с явно указанным "
                + "периодом (from/to/granularity) — с 1 января текущего календарного года по сегодняшний "
                + "день; 1 января следующего года период автоматически начинается заново с января нового "
                + "года.")
        TimeSeriesResponse dynamics,

        @Schema(description = "ТОП-5 диагнозов (МКБ-10) по количеству случаев, за всё время.")
        List<TopDiagnosisResponse> topDiagnoses,

        @Schema(description = "Разбивка случаев (форма №058 + форма №058-1) по источнику поступления, за всё время.")
        List<SourceCountResponse> sourceBreakdown,

        @Schema(description = "Географический разрез за всё время: по районам — если область видимости "
                + "региональная; по регионам — если область видимости республиканская; пустой список — если "
                + "область видимости ограничена одним районом или одной организацией.")
        List<GeoBreakdownItemResponse> geoBreakdown,

        @Schema(description = "Количество медицинских учреждений в рамках доступа текущей организации "
                + "(текущий снимок, не временной ряд).")
        long medicalInstitutionsCount,

        @Schema(description = "Статистика по картам расследования, включая собственную динамику по месяцам.")
        CardStatsResponse cardStats,

        @Schema(description = "Статистика по актам, включая собственную динамику по месяцам.")
        ActStatsResponse actStats
) {
}
