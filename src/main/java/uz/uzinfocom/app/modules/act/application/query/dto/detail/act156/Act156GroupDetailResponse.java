package uz.uzinfocom.app.modules.act.application.query.dto.detail.act156;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;

@Schema(description = "Сведения об организации группового питания (детсад/лагерь и т.п.), проверенные в рамках акта 156.")
public record Act156GroupDetailResponse(
        Long id,
        @Schema(description = "Guruh raqami (masalan, «5-A»).")
        String groupNumber,
        String fullNameOfEducator,
        CitizenLookupType identifierTypeOfEducator,
        String identifierValueOfEducator,
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
        String fullNameOfPlaceOwner,
        CitizenLookupType identifierTypeOfPlaceOwner,
        String identifierValueOfPlaceOwner,
        Boolean bedClothes,
        Boolean bathroomWall,
        Boolean towels,
        Boolean towelRack,
        Boolean waterTapFaucet,
        Boolean wcSeats
) {
}
