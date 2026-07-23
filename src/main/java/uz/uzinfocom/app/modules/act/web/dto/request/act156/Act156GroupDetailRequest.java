package uz.uzinfocom.app.modules.act.web.dto.request.act156;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Сведения об организации группового питания (детсад/лагерь и т.п.), проверяемые в рамках акта 156.")
public record Act156GroupDetailRequest(
        Long id,
        @Size(max = 255) String fullNameOfEducator,
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
        Boolean bedClothes,
        Boolean bathroomWall,
        Boolean towels,
        Boolean towelRack,
        Boolean waterTapFaucet,
        Boolean wcSeats
) implements ChildRequest {
}
