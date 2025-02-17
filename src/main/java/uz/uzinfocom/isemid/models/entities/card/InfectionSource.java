package uz.uzinfocom.isemid.models.entities.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card161_infection_source")
public class InfectionSource extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "card161_id", nullable = false)
    @JsonIgnore
    private Card161 card161;
    private String fullName;
    private String diagnosisClinicalFormOrDonorStatus;
    private String contactInfoAndDonorResidence;
    private String testResult;
}
