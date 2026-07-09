package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

public record OutbreakDisinfectionMeasureRequest(
        Long id,
        @Size(max = 64) String preventiveMeasuresCode,
        @Size(max = 255) String drugType,
        @Size(max = 255) String conductedAt,
        @Size(max = 64) String conductedLocationCode,
        @Size(max = 500) String executors,
        @Size(max = 500) String executionMonitoringResult
) implements ChildRequest {
}
