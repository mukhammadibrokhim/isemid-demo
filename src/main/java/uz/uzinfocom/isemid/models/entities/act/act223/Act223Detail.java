package uz.uzinfocom.isemid.models.entities.act.act223;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act223_details")
public class Act223Detail extends BaseEntity {
    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    private String supportingDocumentsForSampling;

}
