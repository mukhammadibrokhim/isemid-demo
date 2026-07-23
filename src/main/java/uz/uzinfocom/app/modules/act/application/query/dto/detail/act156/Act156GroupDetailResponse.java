package uz.uzinfocom.app.modules.act.application.query.dto.detail.act156;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения об организации группового питания (детсад/лагерь и т.п.), проверенные в рамках акта 156.")
public record Act156GroupDetailResponse(
        Long id,
        String fullNameOfEducator,
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
        Boolean bedClothes,
        Boolean bathroomWall,
        Boolean towels,
        Boolean towelRack,
        Boolean waterTapFaucet,
        Boolean wcSeats
) {
}
