package uz.uzinfocom.app.modules.card.domain.model.card205;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

/**
 * Information about people who own animals bitten by the same source
 * animal. Legacy class name ({@code InformationAboutAnimaBittenPeople} —
 * missing the 'l' in "Animal") is preserved as-is: it is a persisted class
 * name only insofar as JPA needs a Java identifier, and renaming it here
 * carries no behavioral benefit — the field name on {@link Card205}
 * ({@code infoAbtAnimalBittenPeople}) is what the API surface actually uses.
 */
@Getter
@Setter
@Entity
@Table(
        name = "card205_info_about_animal_bitten_people",
        indexes = @Index(name = "idx_card205_info_abt_animal_bitten_people_card205_id", columnList = "card205_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class InformationAboutAnimaBittenPeople extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card205_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card205_info_abt_animal_bitten_people_card205"))
    private Card205 card205;

    @CatalogCode("card205-animal-category")
    @Column(name = "animal_category_code", length = 64)
    private String animalCategoryCode;

    @Column(name = "animal_type", length = 255)
    private String animalType;

    @Column(name = "full_name_of_animal_bitten_owner", length = 255)
    private String fullNameOfAnimalBittenOwner;

    @Column(name = "address_of_animal_bitten_owner", length = 500)
    private String addressOfAnimalBittenOwner;

    @Embedded
    private AdministrativeAddress location;
}
