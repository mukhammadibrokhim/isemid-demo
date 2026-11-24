package uz.uzinfocom.app.modules.act.web.dto.request.act156;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.modules.act.web.dto.request.validation.HasPersonIdentifiers;
import uz.uzinfocom.app.modules.act.web.dto.request.validation.PersonIdentifier;
import uz.uzinfocom.app.modules.act.web.dto.request.validation.ValidPersonIdentifiers;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.util.List;

@ValidPersonIdentifiers
@Schema(description = "Сведения об организации группового питания (детсад/лагерь и т.п.), проверяемые в рамках акта 156.")
public record Act156GroupDetailRequest(
        @Schema(description = "Mavjud qatorni yangilash uchun ID. Guruh raqami emas — u groupNumber'da.")
        Long id,
        @Schema(description = "Guruh raqami (masalan, «5-A»).")
        @Size(max = 50) String groupNumber,
        @Size(max = 255) String fullNameOfEducator,
        @Schema(description = "Тип документа воспитателя («Hujjat turi»), по которому его данные получены из реестра граждан. "
                + "Необязателен, но указывается вместе с identifierValueOfEducator.")
        CitizenLookupType identifierTypeOfEducator,
        @Schema(description = "Номер документа воспитателя («Hujjat raqami» — ПИНФЛ или паспорт). Для NNUZB — ровно 14 цифр.")
        @Size(max = 100) String identifierValueOfEducator,
        Boolean handsOfEducator,
        Boolean firstFoodBowl,
        Boolean secondFoodBowl,
        Boolean tables,
        Boolean chairs,
        Boolean windowSill,
        Boolean doorHandles,
        Boolean toys,
        Boolean toyShelf,
        Boolean carpets,
        Boolean clothesRack,
        @Size(max = 255) String fullNameOfPlaceOwner,
        @Schema(description = "Тип документа владельца места («Hujjat turi»), по которому его данные получены из реестра граждан. "
                + "Необязателен, но указывается вместе с identifierValueOfPlaceOwner.")
        CitizenLookupType identifierTypeOfPlaceOwner,
        @Schema(description = "Номер документа владельца места («Hujjat raqami» — ПИНФЛ или паспорт). Для NNUZB — ровно 14 цифр.")
        @Size(max = 100) String identifierValueOfPlaceOwner,
        Boolean bedClothes,
        Boolean bathroomWall,
        Boolean towels,
        Boolean towelRack,
        Boolean waterTapFaucet,
        Boolean wcSeats
) implements ChildRequest, HasPersonIdentifiers {

    @Override
    public List<PersonIdentifier> personIdentifiers() {
        return List.of(
                new PersonIdentifier("identifierTypeOfEducator", identifierTypeOfEducator,
                        "identifierValueOfEducator", identifierValueOfEducator),
                new PersonIdentifier("identifierTypeOfPlaceOwner", identifierTypeOfPlaceOwner,
                        "identifierValueOfPlaceOwner", identifierValueOfPlaceOwner)
        );
    }
}
