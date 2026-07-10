package uz.uzinfocom.app.modules.form0581.application.command;

import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

public record OtherInjuredPersonCommand(
        Long id,
        String lastName,
        String firstName,
        String middleName,
        String regionCode,
        String districtCode,
        String neighborhoodCode,
        String street,
        String houseNumber,
        String apartmentNumber
) implements ChildRequest {
}
