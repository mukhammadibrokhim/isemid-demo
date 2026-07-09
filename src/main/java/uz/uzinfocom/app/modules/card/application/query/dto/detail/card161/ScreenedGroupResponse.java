package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

public record ScreenedGroupResponse(
        Long id,
        String teamName,
        String prophylacticAddress,
        String contactCount,
        String requiredProphylacticSubstance,
        String treatedWithProphylacticSubstance,
        String laboratoryTestConducted
) {
}
