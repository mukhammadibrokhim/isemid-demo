package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Сведения об удалении формы №058-1.")
public record Form0581DeleteDetailResponse(
        @Schema(description = "Признак того, что форма удалена.")
        Boolean deleted,

        @Schema(description = "Дата и время удаления формы.")
        Instant deletedAt,

        @Schema(description = "Идентификатор пользователя, удалившего форму.")
        Long deletedBy,

        @Schema(description = "Причина удаления формы.")
        String deleteReason
) {
}
