package uz.uzinfocom.app.modules.act.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;

@Schema(description = "Количество актов по статусу.")
public record ActStatusCountResponse(
        @Schema(description = "Статус акта.")
        ActStatus status,

        @Schema(description = "Количество актов с этим статусом.")
        long count
) {
}
