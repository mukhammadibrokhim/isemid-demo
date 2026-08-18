package uz.uzinfocom.app.modules.patient.application.command;

import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

import java.time.LocalDate;

/**
 * Update-only shape for correcting one of a patient's existing affiliations -
 * see {@code UpdatePatientAffiliationRequest}'s javadoc for why this is a
 * distinct type from {@code CreatePatientAffiliationCommand} rather than a
 * shared one: matching on update is by {@code id}, not by creating a fresh
 * row, and there's no organizationName/organizationUuid here since the
 * regular (non-integration) shape never accepted those either.
 */
public record UpdatePatientAffiliationCommand(

        Long id,
        AffiliationType type,
        LocalDate lastVisitedDate,
        String regionCode,
        String districtCode,
        Long organizationId,
        String address

) {
}
