package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Сведения о приёме формы №058 получателем.")
public record Form058AcceptDetailResponse(
        @Schema(description = "Идентификатор пользователя, принявшего форму.")
        Long acceptedBy,

        @Schema(description = "Дата и время приёма формы.")
        Instant acceptedAt,

        @Schema(description = "ФИО пользователя, принявшего форму.")
        String acceptedFullName
) {
}
