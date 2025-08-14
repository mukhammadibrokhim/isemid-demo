package uz.uzinfocom.isemid.features.act.dtos.responses.act156;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Act156KitchenUtensilResponse {
    private Long id;
    private Boolean knifeForBread;
    private Boolean fruitCuttingBoard;
    private Boolean distributionTable;
    private Boolean containerForFinishedProducts;
    private String fullNameOfChef;
    private Boolean handsOfChef;
    private Boolean clothesOfChef;
}
