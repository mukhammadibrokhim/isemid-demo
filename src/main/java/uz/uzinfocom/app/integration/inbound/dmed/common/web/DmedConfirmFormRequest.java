package uz.uzinfocom.app.integration.inbound.dmed.common.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Подтверждение формы от DMED с окончательным диагнозом. Организация — по вызывающему.")
public record DmedConfirmFormRequest(
        @Schema(description = "Окончательный диагноз — код МКБ-10 из справочника.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.inbound.final-icd10-code.required}")
        @Size(max = 32, message = "{validation.inbound.final-icd10-code.size}")
        String finalIcd10Code,

        @Schema(description = "Название окончательного диагноза.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.inbound.final-icd10-name.required}")
        @Size(max = 1024, message = "{validation.inbound.final-icd10-name.size}")
        String finalIcd10Name
) {
}
