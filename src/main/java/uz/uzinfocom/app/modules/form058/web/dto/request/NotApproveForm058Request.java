package uz.uzinfocom.app.modules.form058.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на отклонение утверждения формы №058.")
public record NotApproveForm058Request(
        @Schema(description = "Причина отклонения утверждения.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.not-approve-reason.required}")
        @Size(max = 1000, message = "{validation.form058.not-approve-reason.size}")
        String reason
) {
}
