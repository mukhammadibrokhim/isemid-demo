package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Одна строка отчёта «Statistika» — один узел географии (республика / регион / "
        + "район / организация) либо итоговая строка (\"Jami\"). Несёт числа за «Davr A» и, если вызывающий "
        + "запросил сравнение, за «Davr B» — оба независимо посчитаны для одного и того же узла/области "
        + "доступа.")
public record StatisticsNodeResponse(
        @Schema(description = "Код узла: код региона/района, id организации, \"UZ\" для республики или "
                + "\"TOTAL\" для итоговой строки.")
        String code,

        @Schema(description = "Локализованное наименование узла географии.")
        String name,

        @Schema(description = "Есть ли у узла более глубокий уровень (для отображения раскрывающей стрелки).")
        boolean hasChildren,

        @Schema(description = "Числа за первый период («Davr A» — fromA/toA). Всегда присутствует.")
        StatisticsPeriodCountsResponse periodA,

        @Schema(description = "Числа за второй период («Davr B» — fromB/toB), для сравнения. `null`, если "
                + "вызывающий не запросил fromB/toB — тогда сравнения нет, есть только periodA.")
        StatisticsPeriodCountsResponse periodB
) {
}
