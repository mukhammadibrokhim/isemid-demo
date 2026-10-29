package uz.uzinfocom.app.integration.inbound.common.web;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.web.mapper.PatientRequestMapper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = PatientRequestMapper.class)
public interface IntegrationPatientRequestMapper {

    CreatePatientCommand toCommand(IntegrationPatientRequest request);

    // cityCode - DMED's own naming for what the command calls districtCode.
    @Mapping(target = "districtCode", source = "cityCode")
    CreatePatientAffiliationCommand toCommand(IntegrationPatientAffiliationRequest request);
}
