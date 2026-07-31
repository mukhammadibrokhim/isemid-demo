package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Количество случаев за один календарный месяц, с разбивкой по итоговому статусу.")
public record DynamicsPointResponse(
        @Schema(description = "Первый день месяца, к которому относится точка.")
        LocalDate periodStart,

        @Schema(description = "Количество случаев, зарегистрированных за этот месяц.")
        long count,

        @Schema(description = "Из них — со статусом CANCELED (отменено).")
        long canceledCount,

        @Schema(description = "Из них — со статусом APPROVED (утверждено).")
        long approvedCount
) {
}
