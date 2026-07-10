package uz.uzinfocom.app.modules.form058.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на аннулирование формы №058.")
public record CancelForm058Request(
        @Schema(description = "Причина аннулирования формы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.cancel-reason.required}")
        @Size(max = 1000, message = "{validation.form058.cancel-reason.size}")
        String reason
) {
}
