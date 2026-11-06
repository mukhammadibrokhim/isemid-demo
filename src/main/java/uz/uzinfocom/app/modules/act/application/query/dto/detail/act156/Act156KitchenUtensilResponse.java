package uz.uzinfocom.app.modules.act.application.query.dto.detail.act156;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;

@Schema(description = "Сведения о кухонном инвентаре, проверенные в рамках акта 156.")
public record Act156KitchenUtensilResponse(
        Long id,
        Boolean knifeForBread,
        Boolean fruitCuttingBoard,
        Boolean distributionTable,
        Boolean containerForFinishedProducts,
        String fullNameOfChef,
        CitizenLookupType identifierTypeOfChef,
        String identifierValueOfChef,
        Boolean handsOfChef,
        Boolean clothesOfChef
) {
}
