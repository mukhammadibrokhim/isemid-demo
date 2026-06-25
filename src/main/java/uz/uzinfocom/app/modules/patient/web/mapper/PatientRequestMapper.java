package uz.uzinfocom.app.modules.patient.web.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientIdentifierCommand;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientAddressRequest;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientAffiliationRequest;
import uz.uzinfocom.app.modules.patient.web.request.CreatePatientIdentifierRequest;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface PatientRequestMapper {

    CreatePatientCommand toCommand(PatientRequest request);

    CreatePatientIdentifierCommand toCommand(CreatePatientIdentifierRequest request);

    CreatePatientAddressCommand toCommand(CreatePatientAddressRequest request);

    CreatePatientAffiliationCommand toCommand(CreatePatientAffiliationRequest request);
}
