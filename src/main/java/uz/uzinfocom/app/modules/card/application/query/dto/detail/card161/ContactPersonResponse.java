package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

public record ContactPersonResponse(
        Long id,
        String fullName,
        String age,
        String address,
        String jobTypeAndLocation,
        String immunizationStatus,
        String restrictionMeasures
) {
}
