package uz.uzinfocom.app.modules.act.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;

@Schema(description = "Количество актов по типу.")
public record ActTypeCountResponse(
        @Schema(description = "Тип акта.")
        ActType type,

        @Schema(description = "Количество актов этого типа.")
        long count
) {
}
