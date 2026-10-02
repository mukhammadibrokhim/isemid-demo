package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Разбивка по полу для узла географии, отдельно по подтверждённым и неподтверждённым "
        + "(первичным) случаям.")
public record StatisticsGenderBreakdownResponse(
        @Schema(description = "«Ayollar» среди подтверждённых (status = APPROVED) случаев.")
        long confirmedFemale,

        @Schema(description = "«Erkaklar» среди подтверждённых случаев.")
        long confirmedMale,

        @Schema(description = "«Ayollar» среди неподтверждённых (status not in (APPROVED, CANCELED)) случаев.")
        long primaryFemale,

        @Schema(description = "«Erkaklar» среди неподтверждённых случаев.")
        long primaryMale
) {
}
