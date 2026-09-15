package uz.uzinfocom.app.modules.form129.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на отклонение формы №129 получателем.")
public record RejectForm129Request(
        @Schema(description = "Причина отклонения.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form129.reject-reason.required}")
        @Size(max = 1000, message = "{validation.form129.reject-reason.size}")
        String reason
) {
}
