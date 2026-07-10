package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Ð”ÐµÐ·Ð¸Ð½Ñ„ÐµÐºÑ†Ð¸Ð¾Ð½Ð½Ð¾Ðµ Ð¼ÐµÑ€Ð¾Ð¿Ñ€Ð¸ÑÑ‚Ð¸Ðµ, Ð¿Ñ€Ð¾Ð²ÐµÐ´Ñ‘Ð½Ð½Ð¾Ðµ Ð² Ð¾Ñ‡Ð°Ð³Ðµ Ð·Ð°Ð±Ð¾Ð»ÐµÐ²Ð°Ð½Ð¸Ñ.")
public record OutbreakDisinfectionMeasureRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "ÐšÐ¾Ð´ Ð¿Ñ€Ð¾Ñ„Ð¸Ð»Ð°ÐºÑ‚Ð¸Ñ‡ÐµÑÐºÐ¾Ð³Ð¾ Ð¼ÐµÑ€Ð¾Ð¿Ñ€Ð¸ÑÑ‚Ð¸Ñ (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String preventiveMeasuresCode,

        @Schema(description = "Ð¢Ð¸Ð¿ Ð¸ÑÐ¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ð½Ð½Ð¾Ð³Ð¾ Ð´ÐµÐ·Ð¸Ð½Ñ„Ð¸Ñ†Ð¸Ñ€ÑƒÑŽÑ‰ÐµÐ³Ð¾ Ð¿Ñ€ÐµÐ¿Ð°Ñ€Ð°Ñ‚Ð°.")
        @Size(max = 255) String drugType,

        @Schema(description = "ÐœÐµÑÑ‚Ð¾ Ð¿Ñ€Ð¾Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¼ÐµÑ€Ð¾Ð¿Ñ€Ð¸ÑÑ‚Ð¸Ñ.")
        @Size(max = 255) String conductedAt,

        @Schema(description = "ÐšÐ¾Ð´ Ð¼ÐµÑÑ‚Ð° Ð¿Ñ€Ð¾Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¼ÐµÑ€Ð¾Ð¿Ñ€Ð¸ÑÑ‚Ð¸Ñ (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String conductedLocationCode,

        @Schema(description = "Ð˜ÑÐ¿Ð¾Ð»Ð½Ð¸Ñ‚ÐµÐ»Ð¸ Ð¼ÐµÑ€Ð¾Ð¿Ñ€Ð¸ÑÑ‚Ð¸Ñ.")
        @Size(max = 500) String executors,

        @Schema(description = "Ð ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚ ÐºÐ¾Ð½Ñ‚Ñ€Ð¾Ð»Ñ Ð²Ñ‹Ð¿Ð¾Ð»Ð½ÐµÐ½Ð¸Ñ Ð¼ÐµÑ€Ð¾Ð¿Ñ€Ð¸ÑÑ‚Ð¸Ñ.")
        @Size(max = 500) String executionMonitoringResult
) implements ChildRequest {
}
