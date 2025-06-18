package uz.uzinfocom.isemid.features.act.models.act156;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act156_kitchen_utensils")
public class Act156KitchenUtensil extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act156_id")  // Foreign key to Act154
    private Act156 act156;

    private Boolean knifeForBread;
    private Boolean fruitCuttingBoard;
    private Boolean distributionTable;
    private Boolean containerForFinishedProducts;
    private String fullNameOfChef;
    private Boolean handsOfChef;
    private Boolean clothesOfChef;
}
