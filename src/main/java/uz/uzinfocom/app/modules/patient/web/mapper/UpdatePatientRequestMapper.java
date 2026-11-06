package uz.uzinfocom.app.modules.patient.web.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientIdentifierCommand;
import uz.uzinfocom.app.modules.patient.web.request.UpdatePatientAddressRequest;
import uz.uzinfocom.app.modules.patient.web.request.UpdatePatientAffiliationRequest;
import uz.uzinfocom.app.modules.patient.web.request.UpdatePatientIdentifierRequest;
import uz.uzinfocom.app.modules.patient.web.request.UpdatePatientRequest;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface UpdatePatientRequestMapper {

    UpdatePatientCommand toCommand(UpdatePatientRequest request);

    UpdatePatientIdentifierCommand toCommand(UpdatePatientIdentifierRequest request);

    UpdatePatientAddressCommand toCommand(UpdatePatientAddressRequest request);

    UpdatePatientAffiliationCommand toCommand(UpdatePatientAffiliationRequest request);
}
