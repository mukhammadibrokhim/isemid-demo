package uz.uzinfocom.app.modules.patient.application.command;

import java.time.LocalDate;
import java.util.List;

/**
 * Update-only counterpart of {@code CreatePatientCommand} - see
 * {@code UpdatePatientRequest}'s javadoc for why patient editing (from a
 * Form058/Form0581 update) uses its own command/request family instead of
 * reusing the create-side one.
 */
public record UpdatePatientCommand(

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

        List<UpdatePatientIdentifierCommand> identifiers,
        List<UpdatePatientAddressCommand> addresses,
        List<UpdatePatientAffiliationCommand> affiliations

) {

    public UpdatePatientCommand {
        identifiers = immutableCopy(identifiers);
        addresses = immutableCopy(addresses);
        affiliations = immutableCopy(affiliations);
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
