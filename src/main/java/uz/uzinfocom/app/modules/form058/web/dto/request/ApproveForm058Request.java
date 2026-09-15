package uz.uzinfocom.app.modules.form058.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на утверждение формы №058 с итоговым диагнозом.")
public record ApproveForm058Request(
        @Schema(description = "Итоговый (окончательный) код диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.icd10-code.required}")
        @Size(max = 20, message = "{validation.form058.icd10-code.size}")
        String finalIcd10Code,

        @Schema(description = "Итоговое (окончательное) наименование диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.icd10-name.required}")
        @Size(max = 512, message = "{validation.form058.icd10-name.size}")
        String finalIcd10Name
) {
}
