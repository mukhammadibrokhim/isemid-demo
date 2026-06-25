package uz.uzinfocom.app.modules.patient.application.query.dto.detail;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PatientDetailResponse(
        Long id,
        UUID uuid,

        String firstName,
        String lastName,
        String middleName,
        String fullName,

        LocalDate birthDate,
        Integer ageYears,
        Integer ageMonths,
        String genderCode,
        String phoneNumber,

        String kinshipDegree,
        String kinshipFullName,

        String residentialStatusCode,
        String maritalStatusCode,
        String populationTypeCode,
        String categoryCode,
        String professionCode,

        List<PatientIdentifierDetailResponse> identifiers,
        List<PatientAddressDetailResponse> addresses,
        List<PatientAffiliationDetailResponse> affiliations
) {
}