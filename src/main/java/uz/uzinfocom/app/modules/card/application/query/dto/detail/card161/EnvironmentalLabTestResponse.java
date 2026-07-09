package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import java.time.LocalDate;

public record EnvironmentalLabTestResponse(
        Long id,
        LocalDate examinationDate,
        String objectArthropodsAnimals,
        String material,
        String sampleQuantity,
        String testTypeAndResult
) {
}
