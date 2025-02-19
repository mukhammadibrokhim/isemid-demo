package uz.uzinfocom.isemid.models.entities.card.card205;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card205_info_bitten_animals")
public class InformationOtherBittenAnimals extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "card205_id", nullable = false)
    @JsonIgnore
    private Card205 card205;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog bittenAnimalCategory;
    private LocalDateTime bittenDateTime;
    private String whereAnimalBitten;
}
