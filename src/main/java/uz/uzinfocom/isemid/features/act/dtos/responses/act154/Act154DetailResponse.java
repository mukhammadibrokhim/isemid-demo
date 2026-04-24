package uz.uzinfocom.isemid.features.act.dtos.responses.act154;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;
import uz.uzinfocom.isemid.features.act.models.common.ResearchItemTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.SampleTypeInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act154DetailResponse {
    private Long id;
    private ResearchItemTypeInfo researchItemTypeInfo;
    private String sampleName;
private SampleTypeInfo sampleTypeInfo;
    private String samplingPurposeLoinc;
    private LocalDateTime sampleTakenDateTime;
    //Catalog
    private String shiftCode;

    private Long groupSize;
    private Long serialNumberOfGroup;
    private Double sampleWeight;
    private SampleQtUnit sampleWeightUnit;
    private Double sampleVolume;
    private String note;
}
