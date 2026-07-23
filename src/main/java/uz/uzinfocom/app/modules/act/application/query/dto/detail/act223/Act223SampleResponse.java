package uz.uzinfocom.app.modules.act.application.query.dto.detail.act223;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ResearchItemTypeInfoResponse;
import uz.uzinfocom.app.modules.act.domain.enums.LengthUnit;

@Schema(description = "Отдельная проба, отобранная в рамках акта 223.")
public record Act223SampleResponse(
        Long id,
        ResearchItemTypeInfoResponse researchItemTypeInfo,

        @Schema(description = "Точка отбора в формате WKT, например POINT(69.2797 41.3111).")
        String exactLocationPointSampling,

        Double amount,
        Double depthOfObtainedArea,
        LengthUnit depthUnit
) {
}
