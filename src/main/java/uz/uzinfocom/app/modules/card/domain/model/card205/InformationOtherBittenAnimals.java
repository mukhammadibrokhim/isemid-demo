package uz.uzinfocom.app.modules.card.domain.model.card205;

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
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "card205_info_bitten_animals",
        indexes = @Index(name = "idx_card205_info_bitten_animals_card205_id", columnList = "card205_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class InformationOtherBittenAnimals extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card205_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card205_info_bitten_animals_card205"))
    private Card205 card205;

    @CatalogCode("card205-bitten-animal-category")
    @Column(name = "bitten_animal_category_code", length = 64)
    private String bittenAnimalCategoryCode;

    @Column(name = "bitten_date_time")
    private LocalDateTime bittenDateTime;

    @Column(name = "where_animal_bitten", length = 500)
    private String whereAnimalBitten;
}
