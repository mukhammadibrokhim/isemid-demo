package uz.uzinfocom.app.modules.patient.application.command;

import java.time.LocalDate;
import java.util.List;

public record CreatePatientCommand(

        String firstName,
        String lastName,
        String middleName,
        LocalDate birthDate,
        String genderCode,
        String phoneNumber,

        String kinshipDegree,
        String kinshipFullName,

        String residentialStatusCode,
        String maritalStatusCode,
        String populationTypeCode,
        String categoryCode,
        String professionCode,

        List<CreatePatientIdentifierCommand> identifiers,
        List<CreatePatientAddressCommand> addresses,
        List<CreatePatientAffiliationCommand> affiliations

) {

    public CreatePatientCommand {
        identifiers = immutableCopy(identifiers);
        addresses = immutableCopy(addresses);
        affiliations = immutableCopy(affiliations);
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}