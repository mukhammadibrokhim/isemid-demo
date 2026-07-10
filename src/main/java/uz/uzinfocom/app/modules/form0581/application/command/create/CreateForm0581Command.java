package uz.uzinfocom.app.modules.form0581.application.command.create;

import uz.uzinfocom.app.modules.form0581.application.command.OtherInjuredPersonCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;

import java.time.LocalDateTime;
import java.util.List;

public record CreateForm0581Command(
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

        CreatePatientCommand patient,

        String source,

        Long senderOrganizationId,
        Long receiverOrganizationId,

        Boolean otherPeopleInjured,
        List<OtherInjuredPersonCommand> otherInjuredPeople,

        LocalDateTime hospitalizedAt,
        Long hospitalOrganizationId,

        String antirabicAssistanceInfo,
        String notifierFullName,
        String receiverFullName,
        LocalDateTime messageSentAt
) {

    public CreateForm0581Command {
        otherInjuredPeople = otherInjuredPeople == null ? List.of() : List.copyOf(otherInjuredPeople);
    }
}
