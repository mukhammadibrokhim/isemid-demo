package uz.uzinfocom.app.modules.report.form11.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Один узел (регион/район/организация) дерева отчёта «Form 11: показатели "
        + "заболеваемости инфекционными и паразитарными болезнями». Абсолютный и интенсивный "
        + "(на koef населения) показатели за выбранный период рядом с тем же периодом год назад "
        + "и приростом в %, плюс городской / сельский / детский (до 18 лет) срезы за текущий период.")
public record Form11ReportNodeResponse(
        @Schema(description = "Код узла: код региона/района либо id организации.")
        String code,

        @Schema(description = "Локализованное наименование узла.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "Абсолютный показатель за тот же период год назад (\"O'tgan yil\").")
        long absPreviousYear,

        @Schema(description = "Абсолютный показатель за выбранный период (\"Joriy yil\").")
        long absCurrentYear,

        @Schema(description = "Прирост абсолютного показателя, % — ((curr - prev) / prev) * 100 "
                + "(prev = 0: curr = 0 → 0, curr > 0 → 100).")
        double absGrowthPercent,

        @Schema(description = "Интенсивный показатель (на koef населения) за тот же период год назад.")
        double intensivePreviousYear,

        @Schema(description = "Интенсивный показатель (на koef населения) за выбранный период.")
        double intensiveCurrentYear,

        @Schema(description = "Прирост интенсивного показателя, %.")
        double intensiveGrowthPercent,

        @Schema(description = "Городское население — абсолютный показатель за выбранный период.")
        long cityAbs,

        @Schema(description = "Городское население — интенсивный показатель за выбранный период.")
        double cityIntensive,

        @Schema(description = "Доля городских случаев, % — cityAbs * 100 / absCurrentYear.")
        double citySharePercent,

        @Schema(description = "Сельское население — абсолютный показатель за выбранный период.")
        long ruralAbs,

        @Schema(description = "Сельское население — интенсивный показатель за выбранный период.")
        double ruralIntensive,

        @Schema(description = "Доля сельских случаев, % — ruralAbs * 100 / absCurrentYear.")
        double ruralSharePercent,

        @Schema(description = "Дети до 18 лет — абсолютный показатель за выбранный период.")
        long childAbs,

        @Schema(description = "Дети до 18 лет — интенсивный показатель за выбранный период.")
        double childIntensive
) {
}
