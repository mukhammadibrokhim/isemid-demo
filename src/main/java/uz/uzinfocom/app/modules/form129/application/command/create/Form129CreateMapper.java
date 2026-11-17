package uz.uzinfocom.app.modules.form129.application.command.create;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public abstract class Form129CreateMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "status", constant = "SENT")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "reportingInstitutionName", source = "reportingInstitutionName")
    @Mapping(target = "medicalId", source = "medicalId")
    @Mapping(target = "patient", ignore = true)

    @Mapping(target = "senderOrganizationId", source = "senderOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "receiverOrganizationId")
    @Mapping(target = "sourceIntegrationClientId", source = "sourceIntegrationClientId")

    @Mapping(target = "labResults.rwOutcome", source = "rwOutcome")
    @Mapping(target = "labResults.rwResultText", source = "rwResultText")
    @Mapping(target = "labResults.rprVdrlOutcome", source = "rprVdrlOutcome")
    @Mapping(target = "labResults.rprVdrlResultText", source = "rprVdrlResultText")
    @Mapping(target = "labResults.rpgaOutcome", source = "rpgaOutcome")
    @Mapping(target = "labResults.rpgaResultText", source = "rpgaResultText")
    @Mapping(target = "labResults.elisaOutcome", source = "elisaOutcome")
    @Mapping(target = "labResults.elisaResultText", source = "elisaResultText")
    @Mapping(target = "labResults.tphaOutcome", source = "tphaOutcome")
    @Mapping(target = "labResults.tphaResultText", source = "tphaResultText")
    @Mapping(target = "labResults.westernBlotOutcome", source = "westernBlotOutcome")
    @Mapping(target = "labResults.westernBlotResultText", source = "westernBlotResultText")
    @Mapping(target = "labResults.hbsAgOutcome", source = "hbsAgOutcome")
    @Mapping(target = "labResults.hbsAgResultText", source = "hbsAgResultText")
    @Mapping(target = "labResults.hbeAgOutcome", source = "hbeAgOutcome")
    @Mapping(target = "labResults.hbeAgResultText", source = "hbeAgResultText")
    @Mapping(target = "labResults.antiHbcIgGOutcome", source = "antiHbcIgGOutcome")
    @Mapping(target = "labResults.antiHbcIgGResultText", source = "antiHbcIgGResultText")
    @Mapping(target = "labResults.antiHbcIgMOutcome", source = "antiHbcIgMOutcome")
    @Mapping(target = "labResults.antiHbcIgMResultText", source = "antiHbcIgMResultText")
    @Mapping(target = "labResults.antiHbeOutcome", source = "antiHbeOutcome")
    @Mapping(target = "labResults.antiHbeResultText", source = "antiHbeResultText")
    @Mapping(target = "labResults.antiHbsOutcome", source = "antiHbsOutcome")
    @Mapping(target = "labResults.antiHbsResultText", source = "antiHbsResultText")
    @Mapping(target = "labResults.pcrQualitativeOutcome", source = "pcrQualitativeOutcome")
    @Mapping(target = "labResults.pcrQualitativeResultText", source = "pcrQualitativeResultText")
    @Mapping(target = "labResults.wrightHeddelsonOutcome", source = "wrightHeddelsonOutcome")
    @Mapping(target = "labResults.wrightHeddelsonResultText", source = "wrightHeddelsonResultText")

    @Mapping(target = "notifierFullName", source = "notifierFullName")
    @Mapping(target = "receiverFullName", ignore = true)
    @Mapping(target = "cancellationInfo", ignore = true)
    public abstract Form129 toEntity(CreateForm129Command command);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "status", source = "status")
    public abstract CreateForm129Result toResult(Form129 form129);
}
