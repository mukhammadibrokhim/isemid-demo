package uz.uzinfocom.app.modules.act.web.dto.request.act224;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDateTime;

@Schema(description = "Рекомендованное мероприятие в рамках акта 224.")
public record Act224RecommendationRequest(
        Long id,
        String recommendedActivities,
        LocalDateTime executionPeriod
) implements ChildRequest {
}
