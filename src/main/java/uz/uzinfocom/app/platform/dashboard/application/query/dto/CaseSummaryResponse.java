package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Сводка по эпидемиологическим случаям (формы №058 и №058-1 вместе) в рамках доступа "
        + "текущей организации.")
public record CaseSummaryResponse(
        @Schema(description = "Общее количество случаев (форма №058 + форма №058-1) за всё время, без "
                + "ограничения по дате — см. верхнеуровневое поле generatedAt для момента расчёта.")
        long totalCases,

        @Schema(description = "Количество случаев за всё время, решение по которым ещё не принято "
                + "(статус в процессе).")
        long activeCases,

        @Schema(description = "Количество случаев, зарегистрированных именно в день asOfDate (см. ниже).")
        long newCasesToday,

        @Schema(description = "Дата, за которую посчитано newCasesToday — часовой пояс Asia/Tashkent. "
                + "Явно указана здесь, а не подразумевается по умолчанию, поскольку в отличие от остальных "
                + "полей этой сводки (которые считаются за всё время) newCasesToday — единственное поле, "
                + "ограниченное одним конкретным днём.")
        LocalDate asOfDate,

        @Schema(description = "Количество случаев по форме №058, за всё время.")
        long form058Total,

        @Schema(description = "Количество случаев по форме №058-1, за всё время.")
        long form0581Total
) {
}
