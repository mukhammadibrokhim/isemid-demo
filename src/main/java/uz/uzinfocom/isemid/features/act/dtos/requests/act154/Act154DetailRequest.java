package uz.uzinfocom.isemid.features.act.dtos.requests.act154;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    private SampleTypeInfo sampleTypeInfo;
    private String shiftCode;

    private Long groupSize;
    private Long serialNumberOfGroup;
    private Double sampleWeight;
    private Double sampleVolume;
    private Long docNumberOfTakenObject;
    private String documentConfirmSampling;
    private String note;
}
