package uz.uzinfocom.app.integration.inbound.common.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The person on whose behalf an external system submits a form. An M2M token
 * only proves which system called, so the individual is named in the body and
 * kept in {@code inbound_submission_audit} for audit purposes.
 */
@Schema(description = "Сведения о сотруднике, от имени которого внешняя система отправляет форму "
        + "(для аудита). Обязательно для клиентов с типом EXTERNAL_TOKEN.")
public record InboundSubmitterRequest(
        @Schema(description = "ПИНФЛ сотрудника.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.inbound.submitter.pinfl.required}")
        @Size(max = 32, message = "{validation.inbound.submitter.pinfl.size}")
        String pinfl,

        @Schema(description = "Ф.И.О. сотрудника.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.inbound.submitter.full-name.required}")
        @Size(max = 255, message = "{validation.inbound.submitter.full-name.size}")
        String fullName,

        @Schema(description = "Должность сотрудника.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.inbound.submitter.position.required}")
        @Size(max = 255, message = "{validation.inbound.submitter.position.size}")
        String position,

        @Schema(description = "Телефон сотрудника.")
        @Size(max = 64, message = "{validation.inbound.submitter.phone.size}")
        String phone
) {
}
