package uz.uzinfocom.app.modules.patient.application.command;

import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePatientAffiliationCommand(

        AffiliationType type,
        LocalDate lastVisitedDate,
        String organizationName,
        String regionCode,
        String cityCode,
        Long organizationId,
        UUID organizationUuid,
        String address

) {
}