package uz.uzinfocom.app.modules.form129.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129TestOutcome;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129WrightHeddelsonOutcome;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.PatientDetailResponse;
import uz.uzinfocom.app.platform.persistence.audit.AuditResponse;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Полные детальные сведения о форме №129.")
public record Form129DetailResponse(
        Long id,
        UUID uuid,
        Form129Status status,
        String source,

        String reportingInstitutionName,
        String medicalId,

        Long senderOrganizationId,
        Long receiverOrganizationId,

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

        String notifierFullName,
        String receiverFullName,

        String cancelReason,
        Long canceledBy,
        Instant canceledAt,

        PatientDetailResponse patient,
        AuditResponse audit
) {
}
