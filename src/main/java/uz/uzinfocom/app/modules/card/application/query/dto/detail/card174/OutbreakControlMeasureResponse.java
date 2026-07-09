package uz.uzinfocom.app.modules.card.application.query.dto.detail.card174;

public record OutbreakControlMeasureResponse(
        Long id,
        Integer vaccinatedAnimals,
        Integer lostAnimals,
        Integer meatDelivered,
        String processingMethodCode,
        Integer processedArea,
        Boolean eventConducted
) {
}
