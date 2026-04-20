package uz.uzinfocom.isemid.features.act.dtos.responses.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act154DetailResponse {
    private Long id;
    private Integer itemTypeId;
    private String sampleName;
    private Integer sampleTypeId;
    private String sampleType;
    private Integer samplingPurposeId;
    private String purposeOfSampling;
    private String samplingPurposeLoinc;
    private LocalDateTime sampleTakenDate;
    //Catalog
    private String shiftCode;
    private Long lisOrganizationId;
    private String laboratoryAddress;
    private Integer deliveryConditionId;
    private String deliveryTerm;
    private Integer storageConditionId;
    private String storageCondition;
    private String manufacturingCompany;
    private Date manufactureDate;
    private Long groupSize;
    private Long serialNumberOfGroup;
    private Double sampleWeight;
    private SampleQtUnit sampleWeightUnit;
    private Double sampleVolume;
    private Long docNumberOfTakenObject;
    private Integer packageTypeId;
    private String packageType;
    private String documentConfirmSampling;
    private String purposeOfChecking;
    private String note;
}
