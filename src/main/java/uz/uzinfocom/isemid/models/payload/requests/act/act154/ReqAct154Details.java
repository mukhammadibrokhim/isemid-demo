package uz.uzinfocom.isemid.models.payload.requests.act.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqAct154Details {
    private Long id;
    private String sampleName;
    private String sampleType;
    private String purposeOfSampling;
    private String samplingPurposeLoinc;
    private LocalDateTime sampleTakenDate;
    //Catalog
    private String shiftCode;
    //lisdan keladi organization
    private Long organizationId;
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
