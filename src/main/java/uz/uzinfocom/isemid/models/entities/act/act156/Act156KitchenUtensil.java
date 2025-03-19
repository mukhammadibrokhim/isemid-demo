package uz.uzinfocom.isemid.models.entities.act.act156;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act156_kitchen_utensils")
public class Act156KitchenUtensil extends BaseEntity {
    private Boolean knifeForBread;
    private Boolean fruitCuttingBoard;
    private Boolean distributionTable;
    private Boolean containerForFinishedProducts;
    private String fullNameOfChef;
    private Boolean handsOfChef;
    private Boolean clothesOfChef;
}
