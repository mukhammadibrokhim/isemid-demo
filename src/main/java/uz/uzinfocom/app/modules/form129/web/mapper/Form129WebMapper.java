package uz.uzinfocom.app.modules.form129.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form129.application.command.Form129StatusResult;
import uz.uzinfocom.app.modules.form129.application.command.accept.AcceptForm129Command;
import uz.uzinfocom.app.modules.form129.application.command.create.CreateForm129Command;
import uz.uzinfocom.app.modules.form129.application.command.create.CreateForm129Result;
import uz.uzinfocom.app.modules.form129.application.command.reject.RejectForm129Command;
import uz.uzinfocom.app.modules.form129.web.dto.request.AcceptForm129Request;
import uz.uzinfocom.app.modules.form129.web.dto.request.CreateForm129Request;
import uz.uzinfocom.app.modules.form129.web.dto.request.RejectForm129Request;
import uz.uzinfocom.app.modules.form129.web.dto.response.CreateForm129Response;
import uz.uzinfocom.app.modules.form129.web.dto.response.Form129StatusResponse;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.modules.patient.web.mapper.PatientRequestMapper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = {OrganizationMappingHelper.class, PatientRequestMapper.class})
public interface Form129WebMapper {

    @Mapping(target = "source", source = "source")
    @Mapping(target = "reportingInstitutionName", source = "request.reportingInstitutionName")
    @Mapping(target = "medicalId", source = "request.medicalId")
    @Mapping(target = "patient", source = "request.patient")

    @Mapping(target = "senderOrganizationId", source = "request.senderOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "sourceIntegrationClientId", ignore = true)

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
    CreateForm129Command toCommand(CreateForm129Request request, String source);

    CreateForm129Response toResponse(CreateForm129Result result);

    @Mapping(target = "formId", source = "id")
    @Mapping(target = "receiverFullName", source = "request.receiverFullName")
    AcceptForm129Command toCommand(Long id, AcceptForm129Request request);

    @Mapping(target = "formId", source = "id")
    @Mapping(target = "reason", source = "request.reason")
    RejectForm129Command toCommand(Long id, RejectForm129Request request);

    Form129StatusResponse toResponse(Form129StatusResult result);
}
