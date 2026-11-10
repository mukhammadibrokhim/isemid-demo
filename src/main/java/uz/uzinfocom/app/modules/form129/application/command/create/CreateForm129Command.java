package uz.uzinfocom.app.modules.form129.application.command.create;

import uz.uzinfocom.app.modules.form129.domain.enums.Form129TestOutcome;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129WrightHeddelsonOutcome;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;

public record CreateForm129Command(
        String source,

        String reportingInstitutionName,
        String medicalId,

        CreatePatientCommand patient,

        Long senderOrganizationId,
        Long receiverOrganizationId,
        Long sourceIntegrationClientId,

        Form129TestOutcome rwOutcome,
        String rwResultText,

        Form129TestOutcome rprVdrlOutcome,
        String rprVdrlResultText,

        Form129TestOutcome rpgaOutcome,
        String rpgaResultText,

        Form129TestOutcome elisaOutcome,
        String elisaResultText,

        Form129TestOutcome tphaOutcome,
        String tphaResultText,

        Form129TestOutcome westernBlotOutcome,
        String westernBlotResultText,

        Form129TestOutcome hbsAgOutcome,
        String hbsAgResultText,

        Form129TestOutcome hbeAgOutcome,
        String hbeAgResultText,

        Form129TestOutcome antiHbcIgGOutcome,
        String antiHbcIgGResultText,

        Form129TestOutcome antiHbcIgMOutcome,
        String antiHbcIgMResultText,

        Form129TestOutcome antiHbeOutcome,
        String antiHbeResultText,

        Form129TestOutcome antiHbsOutcome,
        String antiHbsResultText,

        Form129TestOutcome pcrQualitativeOutcome,
        String pcrQualitativeResultText,

        Form129WrightHeddelsonOutcome wrightHeddelsonOutcome,
        String wrightHeddelsonResultText,

        String notifierFullName
) {
}
