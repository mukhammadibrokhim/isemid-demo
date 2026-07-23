package uz.uzinfocom.app.modules.act.web.dto.request.act223;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.domain.enums.LengthUnit;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ResearchItemTypeInfoRequest;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Отдельная проба, отбираемая в рамках акта 223.")
public record Act223SampleRequest(
        Long id,
        ResearchItemTypeInfoRequest researchItemTypeInfo,

        @Schema(description = "Точка отбора в формате WKT, например POINT(69.2797 41.3111).")
        String exactLocationPointSampling,

        Double amount,
        Double depthOfObtainedArea,
        LengthUnit depthUnit
) implements ChildRequest {
}
