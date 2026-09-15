package uz.uzinfocom.app.integration.inbound.form129.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.integration.inbound.common.web.IntegrationPatientRequest;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129TestOutcome;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129WrightHeddelsonOutcome;

import java.util.UUID;

/**
 * Inbound-integration equivalent of {@code CreateForm129Request} — same
 * business fields, minus {@code senderOrganizationId}: the sending
 * organization is never taken from the request body here, only from the
 * authenticated caller (see {@code InboundCallerContext}). Served through
 * the generic {@code /integration/v1/{source}/form-129} endpoint for every
 * registered source, DMED included — there is no separate DMED-specific
 * contract for Form129.
 */
@Schema(description = "Запрос на создание формы №129 через интеграционный API.")
public record InboundCreateForm129Request(
        @Schema(description = "Название учреждения, направившего извещение.")
        @Size(max = 500, message = "{validation.form129.reporting-institution-name.size}")
        String reportingInstitutionName,

        @Schema(description = "Медицинский идентификатор учреждения.")
        @Size(max = 128, message = "{validation.form129.medical-id.size}")
        String medicalId,

        @Schema(description = "Сведения о пациенте, по которому направлено извещение.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form129.patient.required}")
        IntegrationPatientRequest patient,

        @Schema(description = "Идентификатор организации-получателя формы. Должна быть учреждением "
                + "санитарно-эпидемиологической службы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{validation.form129.receiver-organization.required}")
        UUID receiverOrganizationId,

        Form129TestOutcome rwOutcome,
        @Size(max = 500) String rwResultText,

        Form129TestOutcome rprVdrlOutcome,
        @Size(max = 500) String rprVdrlResultText,

        Form129TestOutcome rpgaOutcome,
        @Size(max = 500) String rpgaResultText,

        Form129TestOutcome elisaOutcome,
        @Size(max = 500) String elisaResultText,

        Form129TestOutcome tphaOutcome,
        @Size(max = 500) String tphaResultText,

        Form129TestOutcome westernBlotOutcome,
        @Size(max = 500) String westernBlotResultText,

        Form129TestOutcome hbsAgOutcome,
        @Size(max = 500) String hbsAgResultText,

        Form129TestOutcome hbeAgOutcome,
        @Size(max = 500) String hbeAgResultText,

        Form129TestOutcome antiHbcIgGOutcome,
        @Size(max = 500) String antiHbcIgGResultText,

        Form129TestOutcome antiHbcIgMOutcome,
        @Size(max = 500) String antiHbcIgMResultText,

        Form129TestOutcome antiHbeOutcome,
        @Size(max = 500) String antiHbeResultText,

        Form129TestOutcome antiHbsOutcome,
        @Size(max = 500) String antiHbsResultText,

        Form129TestOutcome pcrQualitativeOutcome,
        @Size(max = 500) String pcrQualitativeResultText,

        Form129WrightHeddelsonOutcome wrightHeddelsonOutcome,
        @Size(max = 500) String wrightHeddelsonResultText,

        @Schema(description = "ФИО лица, сообщившего о случае.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form129.notifier-full-name.required}")
        @Size(max = 255, message = "{validation.form129.notifier-full-name.size}")
        String notifierFullName
) {
}
