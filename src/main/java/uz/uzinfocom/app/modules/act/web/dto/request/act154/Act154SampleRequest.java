package uz.uzinfocom.app.modules.act.web.dto.request.act154;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.SampleQtUnit;
import uz.uzinfocom.app.modules.act.domain.enums.SampleVolumeUnit;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ResearchItemTypeInfoRequest;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Отдельная проба, отбираемая в рамках акта 154.")
public record Act154SampleRequest(
        Long id,
        ResearchItemTypeInfoRequest researchItemTypeInfo,
        @Size(max = 100) String shiftCode,
        @Size(max = 255) String sampleName,
        Long groupSize,
        Long serialNumberOfGroup,
        Double sampleWeight,
        SampleQtUnit sampleQtUnit,
        Double sampleVolume,
        SampleVolumeUnit sampleVolumeUnit,
        String note
) implements ChildRequest {
}
