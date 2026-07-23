package uz.uzinfocom.app.modules.act.application.query.dto.detail.act154;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ResearchItemTypeInfoResponse;
import uz.uzinfocom.app.modules.act.domain.enums.SampleQtUnit;
import uz.uzinfocom.app.modules.act.domain.enums.SampleVolumeUnit;

@Schema(description = "Отдельная проба, отобранная в рамках акта 154.")
public record Act154SampleResponse(
        Long id,
        ResearchItemTypeInfoResponse researchItemTypeInfo,
        String shiftCode,
        String sampleName,
        Long groupSize,
        Long serialNumberOfGroup,
        Double sampleWeight,
        SampleQtUnit sampleQtUnit,
        Double sampleVolume,
        SampleVolumeUnit sampleVolumeUnit,
        String note
) {
}
