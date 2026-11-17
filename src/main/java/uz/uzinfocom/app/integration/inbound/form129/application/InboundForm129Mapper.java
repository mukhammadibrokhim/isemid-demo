package uz.uzinfocom.app.integration.inbound.form129.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.integration.inbound.common.web.IntegrationPatientRequestMapper;
import uz.uzinfocom.app.integration.inbound.form129.web.InboundCreateForm129Request;
import uz.uzinfocom.app.modules.form129.application.command.create.CreateForm129Command;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = {OrganizationMappingHelper.class, IntegrationPatientRequestMapper.class})
public interface InboundForm129Mapper {

    @Mapping(target = "source", source = "source")
    @Mapping(target = "reportingInstitutionName", source = "request.reportingInstitutionName")
    @Mapping(target = "medicalId", source = "request.medicalId")
    @Mapping(target = "patient", source = "request.patient")

    @Mapping(target = "senderOrganizationId", source = "senderOrganizationId")
    @Mapping(target = "sourceIntegrationClientId", source = "sourceIntegrationClientId")
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "activeOrganizationId")

    @Mapping(target = "rwOutcome", source = "request.rwOutcome")
    @Mapping(target = "rwResultText", source = "request.rwResultText")
    @Mapping(target = "rprVdrlOutcome", source = "request.rprVdrlOutcome")
    @Mapping(target = "rprVdrlResultText", source = "request.rprVdrlResultText")
    @Mapping(target = "rpgaOutcome", source = "request.rpgaOutcome")
    @Mapping(target = "rpgaResultText", source = "request.rpgaResultText")
    @Mapping(target = "elisaOutcome", source = "request.elisaOutcome")
    @Mapping(target = "elisaResultText", source = "request.elisaResultText")
    @Mapping(target = "tphaOutcome", source = "request.tphaOutcome")
    @Mapping(target = "tphaResultText", source = "request.tphaResultText")
    @Mapping(target = "westernBlotOutcome", source = "request.westernBlotOutcome")
    @Mapping(target = "westernBlotResultText", source = "request.westernBlotResultText")
    @Mapping(target = "hbsAgOutcome", source = "request.hbsAgOutcome")
    @Mapping(target = "hbsAgResultText", source = "request.hbsAgResultText")
    @Mapping(target = "hbeAgOutcome", source = "request.hbeAgOutcome")
    @Mapping(target = "hbeAgResultText", source = "request.hbeAgResultText")
    @Mapping(target = "antiHbcIgGOutcome", source = "request.antiHbcIgGOutcome")
    @Mapping(target = "antiHbcIgGResultText", source = "request.antiHbcIgGResultText")
    @Mapping(target = "antiHbcIgMOutcome", source = "request.antiHbcIgMOutcome")
    @Mapping(target = "antiHbcIgMResultText", source = "request.antiHbcIgMResultText")
    @Mapping(target = "antiHbeOutcome", source = "request.antiHbeOutcome")
    @Mapping(target = "antiHbeResultText", source = "request.antiHbeResultText")
    @Mapping(target = "antiHbsOutcome", source = "request.antiHbsOutcome")
    @Mapping(target = "antiHbsResultText", source = "request.antiHbsResultText")
    @Mapping(target = "pcrQualitativeOutcome", source = "request.pcrQualitativeOutcome")
    @Mapping(target = "pcrQualitativeResultText", source = "request.pcrQualitativeResultText")
    @Mapping(target = "wrightHeddelsonOutcome", source = "request.wrightHeddelsonOutcome")
    @Mapping(target = "wrightHeddelsonResultText", source = "request.wrightHeddelsonResultText")

    @Mapping(target = "notifierFullName", source = "request.notifierFullName")
    CreateForm129Command toCommand(
            InboundCreateForm129Request request, String source, Long senderOrganizationId, Long sourceIntegrationClientId);
}
