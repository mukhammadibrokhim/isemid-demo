package uz.uzinfocom.app.modules.form0581.application.stats.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;

@Schema(description = "Количество форм №058-1 по статусу.")
public record Form0581StatusCountResponse(
        @Schema(description = "Статус формы.")
        Form0581Status status,

        @Schema(description = "Количество форм с данным статусом.")
        long count
) {
}
