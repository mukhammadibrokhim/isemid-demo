package uz.uzinfocom.isemid.models.entities.card.card174;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card174_outbreak_control_measure")
public class OutbreakControlMeasure extends AbsEntity {
    @ManyToOne
    @JoinColumn(name = "card174_id", nullable = false)
    @JsonIgnore
    private Card174 card174;
    private Integer vaccinatedAnimals;
    private Integer lostAnimals;
    private Integer meatDelivered;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "processing_method_code", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog processingMethod;//Ishlov berish usuli
    private Integer processedArea;
    private Boolean eventConducted;
}
