package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import java.time.LocalDateTime;

public record EnvironmentalSourceResponse(
        Long id,
        String foodAndWaterSourceTypes,
        String collectionLocation,
        LocalDateTime collectionTime,
        String usageLocation,
        LocalDateTime usageTime,
        String storageConditions,
        String qualityFeedbackFromPatientAndOthers
) {
}
