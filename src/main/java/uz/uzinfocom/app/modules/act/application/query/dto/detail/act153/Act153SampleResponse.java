package uz.uzinfocom.app.modules.act.application.query.dto.detail.act153;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ResearchItemTypeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.SampleTypeInfoResponse;
import uz.uzinfocom.app.modules.act.domain.enums.LengthUnit;
import uz.uzinfocom.app.modules.act.domain.enums.SampleQtUnit;
import uz.uzinfocom.app.modules.act.domain.enums.SampleVolumeUnit;

@Schema(description = "Отдельная проба, отобранная в рамках акта 153.")
public record Act153SampleResponse(
        Long id,
        ResearchItemTypeInfoResponse researchItemTypeInfo,
        Integer objectTypeId,
        String objectCode,
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
        SampleTypeInfoResponse sampleTypeInfo
) {
}
