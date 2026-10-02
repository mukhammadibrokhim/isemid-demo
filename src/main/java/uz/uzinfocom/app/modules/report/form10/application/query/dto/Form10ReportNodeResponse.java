package uz.uzinfocom.app.modules.report.form10.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One row (регион / район / организация, либо строка "Jami") of the "Form 10"
 * report — «Respublika bo'yicha ma'muriy hududlar kesimida yuqumli kasalliklar
 * bilan kasallanish to'g'risidagi ma'lumotlar». Two column blocks — "Joriy
 * davr" and "Yig'ma" — each with a whole-population and an under-14 triple of
 * (2025 | 2026 | growth %) × (absolute | intensive).
 */
@Schema(description = "Один узел дерева отчёта «Form 10»: абсолютные и интенсивные (на koef населения "
        + "территории) показатели заболеваемости подтверждёнными (status = APPROVED) случаями за "
        + "выбранный период («Joriy davr») и с начала года («Yig'ma»), каждый — в сравнении с тем же "
        + "периодом прошлого года, с отдельным срезом по детям до 14 лет.")
public record Form10ReportNodeResponse(
        @Schema(description = "Код узла: код региона/района либо id организации; \"TOTAL\" — итоговая строка.")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Есть ли более глубокий уровень иерархии (для раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "«Joriy davr» — выбранный период (месяц / квартал / полугодие / 9 месяцев / год).")
        Form10Block current,

        @Schema(description = "«Yig'ma» — с начала года по конец выбранного периода.")
        Form10Block cumulative
) {
}
