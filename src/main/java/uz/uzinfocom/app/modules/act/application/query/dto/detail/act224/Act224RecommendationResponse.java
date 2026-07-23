package uz.uzinfocom.app.modules.act.application.query.dto.detail.act224;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Рекомендованное мероприятие в рамках акта 224.")
public record Act224RecommendationResponse(
        Long id,
        String recommendedActivities,
        LocalDateTime executionPeriod
) {
}
