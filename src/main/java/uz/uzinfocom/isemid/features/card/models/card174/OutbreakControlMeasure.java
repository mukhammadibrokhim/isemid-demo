package uz.uzinfocom.isemid.features.card.models.card174;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "card174_outbreak_control_measure")
public class OutbreakControlMeasure extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card174_id", nullable = false)
    private Card174 card174;

    private Integer vaccinatedAnimals;
    private Integer lostAnimals;
    private Integer meatDelivered;
    private String processingMethodCode;//Ishlov berish usuli
    private Integer processedArea;
    private Boolean eventConducted;
}
