package uz.uzinfocom.app.modules.card.web.dto.request.card174;

import jakarta.validation.constraints.Size;

public record OutbreakControlMeasureRequest(
        Integer vaccinatedAnimals,
        Integer lostAnimals,
        Integer meatDelivered,
        @Size(max = 64) String processingMethodCode,
        Integer processedArea,
        Boolean eventConducted
) {
}
