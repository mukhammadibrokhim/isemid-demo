package uz.uzinfocom.app.modules.form0581.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на отклонение утверждения формы №058-1.")
public record NotApproveForm0581Request(
        @Schema(description = "Причина отклонения утверждения.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form0581.not-approve-reason.required}")
        @Size(max = 1000, message = "{validation.form0581.not-approve-reason.size}")
        String reason
) {
}
