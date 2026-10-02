package uz.uzinfocom.app.modules.form129.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на удаление формы №129 (только для администратора / супер-администратора).")
public record DeleteForm129Request(
        @Schema(description = "Причина удаления.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form129.delete-reason.required}")
        @Size(max = 1000, message = "{validation.form129.delete-reason.size}")
        String reason
) {

    public DeleteForm129Request {
        reason = reason == null ? null : reason.trim();
    }
}
