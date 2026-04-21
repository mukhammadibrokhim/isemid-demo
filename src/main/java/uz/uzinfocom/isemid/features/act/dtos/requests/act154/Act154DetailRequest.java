package uz.uzinfocom.isemid.features.act.dtos.requests.act154;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;
import uz.uzinfocom.isemid.features.act.models.common.ResearchItemTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.SampleTypeInfo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Act154DetailRequest {
    private Long id;
    //    private Integer researchTypeId;
//    private Integer categoryId;
//    private Integer itemTypeId;
//    private Integer itemTypeNameUz;
//    private Integer itemTypeNameRu;
    private ResearchItemTypeInfo researchItemTypeInfo;

    private String sampleName;
    //    private Integer sampleTypeId;
//    private String sampleTypeUz;
//    private String sampleTypeRu;
    private SampleTypeInfo sampleTypeInfo;
//    private Integer samplingPurposeId;
//    private String purposeOfSamplingUz;
//    private String purposeOfSamplingRu;
//    private String samplingPurposeLoinc;
    private LocalDateTime sampleTakenDateTime;
    //Catalog
    private String shiftCode;
    private Long lisOrganizationId;
    private String laboratoryAddress;
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
    private SampleQtUnit sampleWeightUnit;
    private Double sampleVolume;
    private Long docNumberOfTakenObject;
//    private Integer packageTypeId;
//    private String packageTypeUz;
//    private String packageTypeRu;
    private String documentConfirmSampling;
    private String purposeOfChecking;
    private String note;
}
