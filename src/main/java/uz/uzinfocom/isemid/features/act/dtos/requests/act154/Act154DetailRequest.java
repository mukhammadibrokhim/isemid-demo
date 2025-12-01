package uz.uzinfocom.isemid.features.act.dtos.requests.act154;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Act154DetailRequest {
    private Long id;
    private String sampleName;
    private String sampleType;
    private String purposeOfSampling;
    private String samplingPurposeLoinc;
    private LocalDateTime sampleTakenDate;
    //Catalog
    private String shiftCode;
    private Long lisOrganizationId;
    private String laboratoryAddress;
    private String deliveryTerm;
    private String storageCondition;
    private String manufacturingCompany;
    private Date manufactureDate;
    private Long groupSize;
    private Long serialNumberOfGroup;
    private Double sampleWeight;
    private Double sampleVolume;
    private Long docNumberOfTakenObject;
    private String packageType;
    private String documentConfirmSampling;
    private String purposeOfChecking;
    private String note;
}
