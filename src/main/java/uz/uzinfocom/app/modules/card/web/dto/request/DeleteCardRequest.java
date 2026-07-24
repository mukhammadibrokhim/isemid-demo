package uz.uzinfocom.app.modules.card.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на удаление карты.")
public record DeleteCardRequest(

        @Schema(description = "Причина удаления карты.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.card.delete-reason.required}")
        @Size(
                max = 2_000,
                message = "{validation.card.delete-reason.max-length}"
        )
        String reason

) {

    public DeleteCardRequest {
        reason = reason == null ? null : reason.trim();
    }
}
