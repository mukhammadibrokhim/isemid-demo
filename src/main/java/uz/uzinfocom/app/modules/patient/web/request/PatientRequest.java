package uz.uzinfocom.app.modules.patient.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PatientRequest(

        @Size(max = 255)
        String firstName,

        @Size(max = 255)
        String lastName,

        @Size(max = 255)
        String middleName,

        @PastOrPresent
        LocalDate birthDate,

        @Size(max = 64)
        String genderCode,

        @Size(max = 32)
        String phoneNumber,

        @Size(max = 64)
        String kinshipDegree,

        @Size(max = 500)
        String kinshipFullName,

        @Size(max = 64)
        String residentialStatusCode,

        @Size(max = 64)
        String maritalStatusCode,

        @Size(max = 64)
        String populationTypeCode,

        @Size(max = 64)
        String categoryCode,

        @Size(max = 64)
        String professionCode,

        @Valid
        List<CreatePatientIdentifierRequest> identifiers,

        @Valid
        List<CreatePatientAddressRequest> addresses,

        @Valid
        List<CreatePatientAffiliationRequest> affiliations

) {

    public PatientRequest {
        identifiers = immutableCopy(identifiers);
        addresses = immutableCopy(addresses);
        affiliations = immutableCopy(affiliations);
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}