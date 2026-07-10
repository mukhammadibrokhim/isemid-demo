package uz.uzinfocom.app.modules.form0581.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на удаление формы №058-1.")
public record DeleteForm0581Request(

        @Schema(description = "Причина удаления формы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form0581.delete-reason.required}")
        @Size(
                max = 2_000,
                message = "{validation.form0581.delete-reason.max-length}"
        )
        String reason

) {

    public DeleteForm0581Request {
        reason = reason == null ? null : reason.trim();
    }
}
