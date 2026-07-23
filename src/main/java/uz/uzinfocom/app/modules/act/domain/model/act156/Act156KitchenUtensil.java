package uz.uzinfocom.app.modules.act.domain.model.act156;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "act156_kitchen_utensil",
        indexes = @Index(name = "idx_act156_kitchen_utensil_act156_id", columnList = "act156_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Act156KitchenUtensil extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "act156_id", nullable = false, foreignKey = @ForeignKey(name = "fk_act156_kitchen_utensil_act156"))
    private Act156 act156;

    @Column(name = "knife_for_bread")
    private Boolean knifeForBread;

    @Column(name = "fruit_cutting_board")
    private Boolean fruitCuttingBoard;

    @Column(name = "distribution_table")
    private Boolean distributionTable;

    @Column(name = "container_for_finished_products")
    private Boolean containerForFinishedProducts;

    @Column(name = "full_name_of_chef")
    private String fullNameOfChef;

    @Column(name = "hands_of_chef")
    private Boolean handsOfChef;

    @Column(name = "clothes_of_chef")
    private Boolean clothesOfChef;
}
