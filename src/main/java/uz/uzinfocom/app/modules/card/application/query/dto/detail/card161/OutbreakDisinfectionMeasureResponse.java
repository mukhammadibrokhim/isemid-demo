package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

public record OutbreakDisinfectionMeasureResponse(
        Long id,
        String preventiveMeasuresCode,
        String drugType,
        String conductedAt,
        String conductedLocationCode,
        String executors,
        String executionMonitoringResult
) {
}
