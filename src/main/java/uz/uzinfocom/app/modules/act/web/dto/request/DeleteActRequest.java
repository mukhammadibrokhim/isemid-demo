package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на удаление акта.")
public record DeleteActRequest(

        @Schema(description = "Причина удаления акта.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.act.delete-reason.required}")
        @Size(
                max = 2_000,
                message = "{validation.act.delete-reason.max-length}"
        )
        String reason

) {

    public DeleteActRequest {
        reason = reason == null ? null : reason.trim();
    }
}
