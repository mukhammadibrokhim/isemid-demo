package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Данные главного дашборда: сводка по случаям, динамика, ТОП диагнозов, географический "
        + "разрез, тибиотические учреждения, карты и акты — всё в рамках доступа текущей организации.")
public record HomeDashboardResponse(
        @Schema(description = "Область видимости, применённая к этому ответу.")
        DashboardScopeResponse scope,

        @Schema(description = "Сводка по случаям (формы №058 и №058-1).")
        CaseSummaryResponse caseSummary,

        @Schema(description = "Динамика случаев по месяцам (по умолчанию — последние 6 месяцев).")
        List<DynamicsPointResponse> dynamics,

        @Schema(description = "ТОП-5 диагнозов (МКБ-10) по количеству случаев.")
        List<TopDiagnosisResponse> topDiagnoses,

        @Schema(description = "Географический разрез: по районам — если область видимости региональная; "
                + "по регионам — если область видимости республиканская; пустой список — если область "
                + "видимости ограничена одним районом или одной организацией.")
        List<GeoBreakdownItemResponse> geoBreakdown,

        @Schema(description = "Количество медицинских учреждений в рамках доступа текущей организации.")
        long medicalInstitutionsCount,

        @Schema(description = "Статистика по картам расследования.")
        CardStatsResponse cardStats,

        @Schema(description = "Статистика по актам.")
        ActStatsResponse actStats
) {
}
