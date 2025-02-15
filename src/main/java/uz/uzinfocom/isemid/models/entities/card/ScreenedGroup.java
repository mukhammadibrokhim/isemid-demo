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
@Entity(name = "card161_screened_group")
public class ScreenedGroup extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "card161_id", nullable = false)
    @JsonIgnore
    private Card161 card161;
    private String teamName;
    private String prophylacticAddress;
    private String contactCount;
    private String requiredProphylacticSubstance;
    private String treatedWithProphylacticSubstance;
    private String laboratoryTestConducted;
}
