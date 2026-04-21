package uz.uzinfocom.isemid.features.act.models.act154;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;
import uz.uzinfocom.isemid.features.act.models.common.ResearchItemTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.SampleTypeInfo;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "act154_detail")
public class Act154Detail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act154_id")  // Foreign key to Act154
    @JsonBackReference
    private Act154 act154;

//    private Integer researchTypeId;
//    private Integer categoryId;
//
//    @Column(name = "item_type_id")
//    private Integer itemTypeId;
//
//    private String itemTypeNameUz;
//    private String itemTypeNameRu;
    @Embedded
    private ResearchItemTypeInfo researchItemTypeInfo;
    private Long lisOrganizationId;
    private String laboratoryAddress;

    private String shiftCode;
    private String sampleName;
//    private Integer sampleTypeId;
//    private String sampleTypeUz;
//    private String sampleTypeRu;
    @Embedded
    private SampleTypeInfo sampleTypeInfo;
//    private Integer samplingPurposeId;
//    private String purposeOfSamplingUz;
//    private String purposeOfSamplingRu;
//    private String samplingPurposeLoinc;
//    private LocalDateTime sampleTakenDateTime;
//    private Integer deliveryConditionId;
//    private String deliveryConditionUz;
//    private String deliveryConditionRu;
//    private Integer storageConditionId;
//    private String storageConditionUz;
//    private String storageConditionRu;
    private String manufacturingCompany;
    private Date manufactureDate;
    private Long groupSize;
    private Long serialNumberOfGroup;
    private Double sampleWeight;
    @Enumerated(EnumType.STRING)
    private SampleQtUnit sampleWeightUnit;
    private Double sampleVolume;
    private Long docNumberOfTakenObject;
//    private Integer packageTypeId;
//    private String packageTypeUz;
//    private String packageTypeRu;
    private String documentConfirmSampling;
//    private String purposeOfChecking;
    private String note;
}
