package uz.uzinfocom.isemid.features.act.models.act223;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act223_details")
public class Act223Detail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act223_id")
    private Act223 act223;

    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    private String supportingDocumentsForSampling;

}
