package uz.uzinfocom.isemid.features.act.models.act223;

import jakarta.persistence.Embedded;
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
import uz.uzinfocom.isemid.features.act.models.common.ResearchItemTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.SampleTypeInfo;

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

//    private Integer researchTypeId;
//    private Integer categoryId;

//    private Integer itemTypeId;
//    private String itemTypeNameUz;
//    private String itemTypeNameRu;
    @Embedded
    private ResearchItemTypeInfo researchItemTypeInfo;

    // example in wkt format POINT(69.2797 41.3111)
    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    @Enumerated(EnumType.STRING)
    private LengthUnit depthUnit;

//    private Integer deliveryConditionId;
//    @Column(columnDefinition = "TEXT")
//    private String deliveryConditionsUz;
//    private String deliveryConditionsRu;
//
//    private Integer storageConditionId;
//    @Column(columnDefinition = "TEXT")
//    private String storageConditionsUz;
//    private String storageConditionsRu;

//    private Integer sampleTypeId;
//    @Column(columnDefinition = "TEXT")
//    private String sampleTypeUz;
//    private String sampleTypeRu;
    @Embedded
    private SampleTypeInfo sampleTypeInfo;

    private String supportingDocumentsForSampling;

}
