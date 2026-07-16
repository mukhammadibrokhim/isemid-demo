package uz.uzinfocom.app.modules.form058.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

@Schema(description = "Количество форм №058 по статусу.")
public record Form058StatusCountResponse(
        @Schema(description = "Статус формы.")
        FormStatus status,

        @Schema(description = "Количество форм с данным статусом.")
        long count
) {
}
