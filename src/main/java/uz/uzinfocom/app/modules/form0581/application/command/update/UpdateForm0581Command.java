package uz.uzinfocom.app.modules.form0581.application.command.update;

import uz.uzinfocom.app.modules.form0581.application.command.OtherInjuredPersonCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateForm0581Command(
        Long id,

        String mkb10Code,
        String mkb10Name,
        String injuryLocalization,

        LocalDateTime injuryDateTime,
        LocalDateTime dpuVisitDateTime,
        String injuryRegionCode,
        String injuryDistrictCode,
        String injuryAddress,

        String animalCategoryCode,
        String animalColor,
        String animalType,
        String animalBreed,

        String ownerLastName,
        String ownerFirstName,
        String ownerMiddleName,
        String ownerRegionCode,
        String ownerDistrictCode,
        String ownerNeighborhoodCode,
        String ownerStreet,
        String ownerHouseNumber,
        String ownerApartmentNumber,

        Long receiverOrganizationId,

        Boolean otherPeopleInjured,

        /**
         * {@code null} means "leave the existing list untouched" (this field
         * was omitted from the request); a non-null (possibly empty) list
         * means "replace the list with exactly this" — same semantics
         * {@link uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync}
         * expects.
         */
        List<OtherInjuredPersonCommand> otherInjuredPeople,

        LocalDateTime hospitalizedAt,
        Long hospitalOrganizationId,

        String antirabicAssistanceInfo,
        String notifierFullName,
        String receiverFullName,
        LocalDateTime messageSentAt,

        CreatePatientCommand patient
) {
}
