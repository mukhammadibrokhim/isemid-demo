package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Разбивка по возрасту (18 лет — граница) для узла географии, отдельно по "
        + "подтверждённым и неподтверждённым (первичным) случаям.")
public record StatisticsAgeBreakdownResponse(
        @Schema(description = "«18 yoshdan kichiklar» среди подтверждённых (status = APPROVED) случаев.")
        long confirmedUnder18,

        @Schema(description = "«Katta yoshlilar» (18+) среди подтверждённых случаев.")
        long confirmedAdult,

        @Schema(description = "«18 yoshdan kichiklar» среди неподтверждённых (status not in (APPROVED, CANCELED)) случаев.")
        long primaryUnder18,

        @Schema(description = "«Katta yoshlilar» (18+) среди неподтверждённых случаев.")
        long primaryAdult
) {
}
