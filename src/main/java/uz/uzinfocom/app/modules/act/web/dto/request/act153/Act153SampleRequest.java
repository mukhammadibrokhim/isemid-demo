package uz.uzinfocom.app.modules.act.web.dto.request.act153;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.LengthUnit;
import uz.uzinfocom.app.modules.act.domain.enums.SampleQtUnit;
import uz.uzinfocom.app.modules.act.domain.enums.SampleVolumeUnit;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ResearchItemTypeInfoRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.SampleTypeInfoRequest;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Отдельная проба, отбираемая в рамках акта 153.")
public record Act153SampleRequest(
        @Schema(description = "Идентификатор существующей пробы — null для новой.")
        Long id,
        ResearchItemTypeInfoRequest researchItemTypeInfo,
        Integer objectTypeId,
        @Size(max = 255) String objectCode,
        String address,
        Double samplingDepth,
        LengthUnit depthUnit,
        Double distanceFromShore,
        LengthUnit distanceFromShoreUnit,
        Double sampleVolume,
        SampleVolumeUnit sampleVolumeUnit,
        SampleQtUnit sampleQtUnit,
        String sampleLocation,
        Double weatherAtSampling,
        Double waterTemperature,
        SampleTypeInfoRequest sampleTypeInfo
) implements ChildRequest {
}
