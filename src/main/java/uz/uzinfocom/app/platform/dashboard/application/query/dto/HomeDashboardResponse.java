package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Общий обзор дашборда: медицинские учреждения и пользователи (с разбивкой по ролям), "
        + "в рамках доступа текущей организации — текущий снимок, не временной ряд. Статистику по "
        + "конкретному модулю (случаи по форме №058, форме №058-1, карты, акты) см. в /v1/dashboard/home/{module}.")
public record HomeDashboardResponse(
        @Schema(description = "Момент расчёта этого снимка (UTC).")
        Instant generatedAt,

        @Schema(description = "Область видимости, применённая к этому ответу.")
        DashboardScopeResponse scope,

        @Schema(description = "Медицинские учреждения в рамках доступа текущей организации, с разбивкой по "
                + "медицинскому типу и уровню организации.")
        MedicalInstitutionsResponse medicalInstitutions,

        @Schema(description = "Пользователи в рамках доступа текущей организации, с разбивкой по ролям.")
        UsersResponse users
) {
}
