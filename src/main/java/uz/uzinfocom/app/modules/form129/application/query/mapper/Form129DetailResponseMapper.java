package uz.uzinfocom.app.modules.form129.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form129.application.query.dto.detail.Form129DetailResponse;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.patient.application.query.mapper.PatientDetailResponseMapper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;
import uz.uzinfocom.app.platform.persistence.audit.AuditResponse;

@Mapper(
        config = CentralMapperConfig.class,
        uses = {PatientDetailResponseMapper.class}
)
public interface Form129DetailResponseMapper {

    @Mapping(target = "id", source = "form129.id")
    @Mapping(target = "uuid", source = "form129.uuid")
    @Mapping(target = "status", source = "form129.status")
    @Mapping(target = "source", source = "form129.source")

    @Mapping(target = "reportingInstitutionName", source = "form129.reportingInstitutionName")
    @Mapping(target = "medicalId", source = "form129.medicalId")

    @Mapping(target = "senderOrganizationId", source = "form129.senderOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "form129.receiverOrganizationId")

    @Mapping(target = "rwOutcome", source = "form129.labResults.rwOutcome")
    @Mapping(target = "rwResultText", source = "form129.labResults.rwResultText")
    @Mapping(target = "rprVdrlOutcome", source = "form129.labResults.rprVdrlOutcome")
    @Mapping(target = "rprVdrlResultText", source = "form129.labResults.rprVdrlResultText")
    @Mapping(target = "rpgaOutcome", source = "form129.labResults.rpgaOutcome")
    @Mapping(target = "rpgaResultText", source = "form129.labResults.rpgaResultText")
    @Mapping(target = "elisaOutcome", source = "form129.labResults.elisaOutcome")
    @Mapping(target = "elisaResultText", source = "form129.labResults.elisaResultText")
    @Mapping(target = "tphaOutcome", source = "form129.labResults.tphaOutcome")
    @Mapping(target = "tphaResultText", source = "form129.labResults.tphaResultText")
    @Mapping(target = "westernBlotOutcome", source = "form129.labResults.westernBlotOutcome")
    @Mapping(target = "westernBlotResultText", source = "form129.labResults.westernBlotResultText")
    @Mapping(target = "hbsAgOutcome", source = "form129.labResults.hbsAgOutcome")
    @Mapping(target = "hbsAgResultText", source = "form129.labResults.hbsAgResultText")
    @Mapping(target = "hbeAgOutcome", source = "form129.labResults.hbeAgOutcome")
    @Mapping(target = "hbeAgResultText", source = "form129.labResults.hbeAgResultText")
    @Mapping(target = "antiHbcIgGOutcome", source = "form129.labResults.antiHbcIgGOutcome")
    @Mapping(target = "antiHbcIgGResultText", source = "form129.labResults.antiHbcIgGResultText")
    @Mapping(target = "antiHbcIgMOutcome", source = "form129.labResults.antiHbcIgMOutcome")
    @Mapping(target = "antiHbcIgMResultText", source = "form129.labResults.antiHbcIgMResultText")
    @Mapping(target = "antiHbeOutcome", source = "form129.labResults.antiHbeOutcome")
    @Mapping(target = "antiHbeResultText", source = "form129.labResults.antiHbeResultText")
    @Mapping(target = "antiHbsOutcome", source = "form129.labResults.antiHbsOutcome")
    @Mapping(target = "antiHbsResultText", source = "form129.labResults.antiHbsResultText")
    @Mapping(target = "pcrQualitativeOutcome", source = "form129.labResults.pcrQualitativeOutcome")
    @Mapping(target = "pcrQualitativeResultText", source = "form129.labResults.pcrQualitativeResultText")
    @Mapping(target = "wrightHeddelsonOutcome", source = "form129.labResults.wrightHeddelsonOutcome")
    @Mapping(target = "wrightHeddelsonResultText", source = "form129.labResults.wrightHeddelsonResultText")

    @Mapping(target = "notifierFullName", source = "form129.notifierFullName")
    @Mapping(target = "receiverFullName", source = "form129.receiverFullName")

    @Mapping(target = "cancelReason", source = "form129.cancellationInfo.cancelReason")
    @Mapping(target = "canceledBy", source = "form129.cancellationInfo.canceledBy")
    @Mapping(target = "canceledAt", source = "form129.cancellationInfo.canceledAt")

    @Mapping(target = "patient", source = "form129.patient")
    @Mapping(target = "audit", source = "audit")
    Form129DetailResponse toDetailedResponse(Form129 form129, AuditResponse audit);
}
