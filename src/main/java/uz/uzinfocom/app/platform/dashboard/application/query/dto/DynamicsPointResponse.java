package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Количество случаев (форма №058 + форма №058-1) за один календарный месяц.")
public record DynamicsPointResponse(
        @Schema(description = "Первый день месяца, к которому относится точка.")
        LocalDate periodStart,

        @Schema(description = "Количество случаев за этот месяц.")
        long count
) {
}
