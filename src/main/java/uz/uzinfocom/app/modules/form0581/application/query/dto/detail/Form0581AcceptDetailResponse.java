package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Сведения о приёме формы №058-1 получателем.")
public record Form0581AcceptDetailResponse(
        @Schema(description = "Идентификатор пользователя, принявшего форму.")
        Long acceptedBy,

        @Schema(description = "Дата и время приёма формы.")
        Instant acceptedAt,

        @Schema(description = "ФИО пользователя, принявшего форму.")
        String acceptedFullName
) {
}
