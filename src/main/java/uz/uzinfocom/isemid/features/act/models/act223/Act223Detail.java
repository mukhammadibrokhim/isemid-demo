package uz.uzinfocom.isemid.features.act.models.act223;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;

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

    private Integer itemTypeId;
    // example in wkt format POINT(69.2797 41.3111)
    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    @Enumerated(EnumType.STRING)
    private LengthUnit depthUnit;
    @Column(name = "delivery_condition_id")
    private Integer deliveryConditionId;

    @Column(name = "delivery_conditions", columnDefinition = "TEXT")
    private String deliveryConditions;

    @Column(name = "storage_condition_id")
    private Integer storageConditionId;

    @Column(name = "storage_conditions", columnDefinition = "TEXT")
    private String storageConditions;
    @Column(name = "sample_type_id")
    private Integer sampleTypeId;

    @Column(name = "sample_type", columnDefinition = "TEXT")
    private String sampleType;
    private String supportingDocumentsForSampling;

}
