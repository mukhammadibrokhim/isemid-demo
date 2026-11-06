package uz.uzinfocom.app.modules.form129.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129TestOutcome;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129WrightHeddelsonOutcome;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.util.UUID;

@Schema(description = "Запрос на создание формы №129 — извещения лаборатории в СЭС о результатах "
        + "исследований (сифилис, гепатит B, бруцеллёз).")
public record CreateForm129Request(
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
        PatientRequest patient,

        @Schema(description = "Идентификатор организации-отправителя формы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{validation.form129.sender-organization.required}")
        UUID senderOrganizationId,

        @Schema(description = "Идентификатор организации-получателя формы. Должна быть учреждением "
                + "санитарно-эпидемиологической службы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{validation.form129.receiver-organization.required}")
        UUID receiverOrganizationId,

        @Schema(description = "Реакция Вассермана (RW).")
        Form129TestOutcome rwOutcome,
        @Size(max = 500) String rwResultText,

        @Schema(description = "RPR / VDRL.")
        Form129TestOutcome rprVdrlOutcome,
        @Size(max = 500) String rprVdrlResultText,

        @Schema(description = "РПГА.")
        Form129TestOutcome rpgaOutcome,
        @Size(max = 500) String rpgaResultText,

        @Schema(description = "ИФА (ELISA).")
        Form129TestOutcome elisaOutcome,
        @Size(max = 500) String elisaResultText,

        @Schema(description = "TPHA.")
        Form129TestOutcome tphaOutcome,
        @Size(max = 500) String tphaResultText,

        @Schema(description = "Иммуноблот (Western blot).")
        Form129TestOutcome westernBlotOutcome,
        @Size(max = 500) String westernBlotResultText,

        @Schema(description = "HBsAg.")
        Form129TestOutcome hbsAgOutcome,
        @Size(max = 500) String hbsAgResultText,

        @Schema(description = "HBeAg.")
        Form129TestOutcome hbeAgOutcome,
        @Size(max = 500) String hbeAgResultText,

        @Schema(description = "Anti-HBc IgG.")
        Form129TestOutcome antiHbcIgGOutcome,
        @Size(max = 500) String antiHbcIgGResultText,

        @Schema(description = "Anti-HBc IgM.")
        Form129TestOutcome antiHbcIgMOutcome,
        @Size(max = 500) String antiHbcIgMResultText,

        @Schema(description = "Anti-HBe.")
        Form129TestOutcome antiHbeOutcome,
        @Size(max = 500) String antiHbeResultText,

        @Schema(description = "Anti-HBs.")
        Form129TestOutcome antiHbsOutcome,
        @Size(max = 500) String antiHbsResultText,

        @Schema(description = "Метод ПЦР (качественный).")
        Form129TestOutcome pcrQualitativeOutcome,
        @Size(max = 500) String pcrQualitativeResultText,

        @Schema(description = "Реакция Райта-Хеддельсона (бруцеллёз).")
        Form129WrightHeddelsonOutcome wrightHeddelsonOutcome,
        @Size(max = 500) String wrightHeddelsonResultText,

        @Schema(description = "ФИО лица, сообщившего о случае.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form129.notifier-full-name.required}")
        @Size(max = 255, message = "{validation.form129.notifier-full-name.size}")
        String notifierFullName
) {
}
