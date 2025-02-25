package uz.uzinfocom.isemid.models.entities.card.card161;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card161_environmental_source")
public class EnvironmentalSource extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "card161_id", nullable = false)
    @JsonIgnore
    private Card161 card161;
    private String foodAndWaterSourceTypes;
    private String collectionLocation;
    private LocalDateTime collectionTime;
    private String usageLocation;
    private LocalDateTime usageTime;
    private String storageConditions;
    private String qualityFeedbackFromPatientAndOthers;
}
