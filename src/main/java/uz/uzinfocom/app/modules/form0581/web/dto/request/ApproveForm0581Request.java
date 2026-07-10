package uz.uzinfocom.app.modules.form0581.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на утверждение формы №058-1 с итоговым диагнозом.")
public record ApproveForm0581Request(
        @Schema(description = "Итоговый (окончательный) код диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form0581.mkb10-code.required}")
        @Size(max = 20, message = "{validation.form0581.mkb10-code.size}")
        String finalMkb10Code,

        @Schema(description = "Итоговое (окончательное) наименование диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form0581.mkb10-name.required}")
        @Size(max = 512, message = "{validation.form0581.mkb10-name.size}")
        String finalMkb10Name
) {
}
