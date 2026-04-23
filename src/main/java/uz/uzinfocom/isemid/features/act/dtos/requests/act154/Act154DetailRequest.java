package uz.uzinfocom.isemid.features.act.dtos.requests.act154;

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
    private ResearchItemTypeInfo researchItemTypeInfo;
    private String sampleName;
    // remove following
    private SampleTypeInfo sampleTypeInfo;
    // persistence issue not saving
//    private LocalDateTime sampleTakenDateTime;
    private String shiftCode;

    private Long groupSize;
    private Long serialNumberOfGroup;
    private Double sampleWeight;
    private SampleQtUnit sampleWeightUnit;
    private Double sampleVolume;
    private Long docNumberOfTakenObject;
    // move to core
    private String documentConfirmSampling;
//    private String purposeOfChecking;
    private String note;
}
