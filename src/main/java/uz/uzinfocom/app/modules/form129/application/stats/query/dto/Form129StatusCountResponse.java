package uz.uzinfocom.app.modules.form129.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;

@Schema(description = "Количество форм №129 по статусу.")
public record Form129StatusCountResponse(
        @Schema(description = "Статус формы.")
        Form129Status status,

        @Schema(description = "Количество форм с данным статусом.")
        long count
) {
}
