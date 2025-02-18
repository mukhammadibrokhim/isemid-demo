package uz.uzinfocom.isemid.models.entities.card.card161;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card161_environmental_lab_test")
public class EnvironmentalLabTest extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "card161_id", nullable = false)
    @JsonIgnore
    private Card161 card161;

    private LocalDate examinationDate;
    private String objectArthropodsAnimals;
    private String material;
    private String sampleQuantity;
    private String testTypeAndResult;
}
