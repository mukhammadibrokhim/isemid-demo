package uz.uzinfocom.isemid.models.entities.card.card161;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card161_prevent_measure")
public class HomePreventiveMeasure extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "card161_id", nullable = false)
    @JsonIgnore
    private Card161 card161;
    private String notifiedPerson;
    private LocalDateTime nextDiseaseTime; //DMED integration
    private String drugName; //DMED integration
    private String dose; //DMED integration
    private String series; //DMED integration

    private LocalDateTime resultTime; //integration LIS
    private LocalDate receivedDate; // Олинган сана
    private String lisResult;//integration LIS
    private String observationResult;
}
