package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "ÐšÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ðµ Ð»Ð¸Ñ†Ð¾, Ð¿Ð¾Ð´Ð»ÐµÐ¶Ð°Ñ‰ÐµÐµ Ð½Ð°Ð±Ð»ÑŽÐ´ÐµÐ½Ð¸ÑŽ.")
public record ContactPersonRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "Ð¤Ð˜Ðž ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        @Size(max = 500) String fullName,

        @Schema(description = "Ð’Ð¾Ð·Ñ€Ð°ÑÑ‚ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        @Size(max = 32) String age,

        @Schema(description = "ÐÐ´Ñ€ÐµÑ Ð¿Ñ€Ð¾Ð¶Ð¸Ð²Ð°Ð½Ð¸Ñ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        @Size(max = 500) String address,

        @Schema(description = "ÐœÐµÑÑ‚Ð¾ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹/ÑƒÑ‡Ñ‘Ð±Ñ‹ Ð¸ Ð´Ð¾Ð»Ð¶Ð½Ð¾ÑÑ‚ÑŒ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        @Size(max = 500) String jobTypeAndLocation,

        @Schema(description = "Ð¡Ñ‚Ð°Ñ‚ÑƒÑ Ð¸Ð¼Ð¼ÑƒÐ½Ð¸Ð·Ð°Ñ†Ð¸Ð¸ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        @Size(max = 255) String immunizationStatus,

        @Schema(description = "ÐžÐ³Ñ€Ð°Ð½Ð¸Ñ‡Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ñ‹Ðµ Ð¼ÐµÑ€Ñ‹, Ð¿Ñ€Ð¸Ð¼ÐµÐ½Ñ‘Ð½Ð½Ñ‹Ðµ Ð² Ð¾Ñ‚Ð½Ð¾ÑˆÐµÐ½Ð¸Ð¸ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        @Size(max = 500) String restrictionMeasures
) implements ChildRequest {
}
