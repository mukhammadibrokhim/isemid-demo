package uz.uzinfocom.isemid.models.payload.responses.act.act156;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespAct156KitchenUtensil {
    private Boolean knifeForBread;
    private Boolean fruitCuttingBoard;
    private Boolean distributionTable;
    private Boolean containerForFinishedProducts;
    private String fullNameOfChef;
    private Boolean handsOfChef;
    private Boolean clothesOfChef;
}
