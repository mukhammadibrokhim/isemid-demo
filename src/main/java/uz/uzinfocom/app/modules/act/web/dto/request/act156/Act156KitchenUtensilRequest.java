package uz.uzinfocom.app.modules.act.web.dto.request.act156;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Сведения о кухонном инвентаре, проверяемые в рамках акта 156.")
public record Act156KitchenUtensilRequest(
        Long id,
        Boolean knifeForBread,
        Boolean fruitCuttingBoard,
        Boolean distributionTable,
        Boolean containerForFinishedProducts,
        @Size(max = 255) String fullNameOfChef,
        Boolean handsOfChef,
        Boolean clothesOfChef
) implements ChildRequest {
}
