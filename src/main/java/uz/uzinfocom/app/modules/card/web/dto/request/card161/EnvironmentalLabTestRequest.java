package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EnvironmentalLabTestRequest(
        LocalDate examinationDate,
        @Size(max = 500) String objectArthropodsAnimals,
        @Size(max = 255) String material,
        @Size(max = 100) String sampleQuantity,
        @Size(max = 500) String testTypeAndResult
) {
}
