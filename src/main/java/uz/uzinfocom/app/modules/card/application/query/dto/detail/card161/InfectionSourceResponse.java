package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

public record InfectionSourceResponse(
        Long id,
        String fullName,
        String diagnosisClinicalFormOrDonorStatus,
        String contactInfoAndDonorResidence,
        String testResult
) {
}
