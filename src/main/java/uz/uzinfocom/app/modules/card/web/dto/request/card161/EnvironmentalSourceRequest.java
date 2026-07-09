package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

import java.time.LocalDateTime;

public record EnvironmentalSourceRequest(
        Long id,
        @Size(max = 500) String foodAndWaterSourceTypes,
        @Size(max = 500) String collectionLocation,
        LocalDateTime collectionTime,
        @Size(max = 500) String usageLocation,
        LocalDateTime usageTime,
        @Size(max = 500) String storageConditions,
        @Size(max = 1000) String qualityFeedbackFromPatientAndOthers
) implements ChildRequest {
}
