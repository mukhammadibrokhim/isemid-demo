package uz.uzinfocom.isemid.features.act.dtos.responses.act156;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
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
