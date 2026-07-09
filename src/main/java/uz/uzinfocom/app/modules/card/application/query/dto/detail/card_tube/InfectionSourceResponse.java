package uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube;

public record InfectionSourceResponse(
        Long id,
        String tbContactCode,
        String fullName,
        String relationDegreeCode,
        String contactDuration
) {
}
