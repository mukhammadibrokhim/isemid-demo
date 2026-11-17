package uz.uzinfocom.app.integration.inbound.common.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Подтверждение формы с окончательным диагнозом (generic-эндпоинт).")
public record InboundConfirmFormRequest(
        @Schema(description = "Окончательный диагноз — код МКБ-10 из справочника.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.inbound.final-icd10-code.required}")
        @Size(max = 32, message = "{validation.inbound.final-icd10-code.size}")
        String finalIcd10Code,

        @Schema(description = "Название окончательного диагноза.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.inbound.final-icd10-name.required}")
        @Size(max = 1024, message = "{validation.inbound.final-icd10-name.size}")
        String finalIcd10Name,

        @Schema(description = "UUID организации-отправителя формы. Обязателен для клиентов EXTERNAL_TOKEN "
                + "и клиентов без организации по умолчанию.")
        UUID organizationUuid,

        @Schema(description = "Сотрудник, от имени которого отправляется подтверждение. "
                + "Обязателен для клиентов EXTERNAL_TOKEN; если передан — pinfl, fullName и position обязательны.")
        @Valid
        InboundSubmitterRequest submittedBy
) {
}
