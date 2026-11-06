package uz.uzinfocom.app.modules.report.form11.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One row (регион / район / организация, либо строка "Jami") of the "Form 11"
 * report — «Yuqumli va parazitar kasalliklar bilan kasallanish
 * ko'rsatkichlari». Two column blocks — "Joriy davr" and "Yig'ma" — each with
 * a whole-population, urban, rural and under-18 triple of (o'tgan yil |
 * joriy yil | o'sish-pasayish) × (absolute | intensive). Matches {@code
 * Form10ReportNodeResponse}'s shape exactly.
 */
@Schema(description = "Один узел дерева отчёта «Form 11»: абсолютные и интенсивные (на koef населения "
        + "территории) показатели заболеваемости подтверждёнными (status = APPROVED) случаями за "
        + "выбранный период («Joriy davr») и с начала года («Yig'ma»), каждый — в сравнении с тем же "
        + "периодом прошлого года, с отдельными срезами по городскому / сельскому населению и детям до 18 лет.")
public record Form11ReportNodeResponse(
        @Schema(description = "Код узла: код региона/района либо id организации; \"TOTAL\" — итоговая строка.")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Есть ли более глубокий уровень иерархии (для раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "«Joriy davr» — выбранный период (месяц / квартал / полугодие / 9 месяцев / год).")
        Form11Block current,

        @Schema(description = "«Yig'ma» — с начала года по конец выбранного периода.")
        Form11Block cumulative
) {
}
