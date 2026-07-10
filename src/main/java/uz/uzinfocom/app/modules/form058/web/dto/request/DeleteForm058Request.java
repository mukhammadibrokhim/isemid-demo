package uz.uzinfocom.app.modules.form058.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на удаление формы №058.")
public record DeleteForm058Request(

        @Schema(description = "Причина удаления формы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.delete-reason.required}")
        @Size(
                max = 2_000,
                message = "{validation.form058.delete-reason.max-length}"
        )
        String reason

) {

    public DeleteForm058Request {
        reason = reason == null ? null : reason.trim();
    }
}
