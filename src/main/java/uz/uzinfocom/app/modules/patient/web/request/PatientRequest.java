package uz.uzinfocom.app.modules.patient.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PatientRequest(

        @Size(max = 255, message = "{patient.request.first_name.size}")
        String firstName,

        @Size(max = 255, message = "{patient.request.last_name.size}")
        String lastName,

        @Size(max = 255, message = "{patient.request.middle_name.size}")
        String middleName,

        @PastOrPresent(message = "{patient.request.birth_date.past_or_present}")
        LocalDate birthDate,

        @Size(max = 64, message = "{patient.request.gender_code.size}")
        String genderCode,

        @Size(max = 32, message = "{patient.request.phone_number.size}")
        String phoneNumber,

        @Size(max = 64, message = "{patient.request.kinship_degree.size}")
        String kinshipDegree,

        @Size(max = 500, message = "{patient.request.kinship_full_name.size}")
        String kinshipFullName,

        @Size(max = 64, message = "{patient.request.residential_status_code.size}")
        String residentialStatusCode,

        @Size(max = 64, message = "{patient.request.marital_status_code.size}")
        String maritalStatusCode,

        @Size(max = 64, message = "{patient.request.population_type_code.size}")
        String populationTypeCode,

        @Size(max = 64, message = "{patient.request.category_code.size}")
        String categoryCode,

        @Size(max = 64, message = "{patient.request.profession_code.size}")
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
