package uz.uzinfocom.app.modules.patient.application.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.PatientAddressDetailResponse;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.PatientAffiliationDetailResponse;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.PatientDetailResponse;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.PatientIdentifierDetailResponse;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAddress;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PatientDetailResponseMapper {

    PatientDetailResponse toDetailedResponse(Patient patient);

    List<PatientIdentifierDetailResponse> toIdentifierResponses(List<PatientIdentifier> identifiers);

    List<PatientAddressDetailResponse> toAddressResponses(List<PatientAddress> addresses);

    List<PatientAffiliationDetailResponse> toAffiliationResponses(List<PatientAffiliation> affiliations);

    PatientIdentifierDetailResponse toResponse(PatientIdentifier identifier);

    PatientAddressDetailResponse toResponse(PatientAddress address);

    PatientAffiliationDetailResponse toResponse(PatientAffiliation affiliation);
}
