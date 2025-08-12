package uz.uzinfocom.isemid.features.act.models.act223;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Setter
@Getter
@Entity
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "act223_detail")
public class Act223Detail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act223_id")
    private Act223 act223;

    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    private String supportingDocumentsForSampling;

}
