package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "ÐžÐ±ÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð½Ð°Ñ Ð³Ñ€ÑƒÐ¿Ð¿Ð° Ð½Ð°ÑÐµÐ»ÐµÐ½Ð¸Ñ (ÑÐºÑ€Ð¸Ð½Ð¸Ð½Ð³).")
public record ScreenedGroupRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "ÐÐ°Ð¸Ð¼ÐµÐ½Ð¾Ð²Ð°Ð½Ð¸Ðµ Ð¾Ð±ÑÐ»ÐµÐ´Ð¾Ð²Ð°Ñ‚ÐµÐ»ÑŒÑÐºÐ¾Ð¹ Ð±Ñ€Ð¸Ð³Ð°Ð´Ñ‹/ÐºÐ¾Ð¼Ð°Ð½Ð´Ñ‹.")
        @Size(max = 255) String teamName,

        @Schema(description = "ÐÐ´Ñ€ÐµÑ Ð¿Ñ€Ð¾Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¿Ñ€Ð¾Ñ„Ð¸Ð»Ð°ÐºÑ‚Ð¸Ñ‡ÐµÑÐºÐ¾Ð³Ð¾ Ð¼ÐµÑ€Ð¾Ð¿Ñ€Ð¸ÑÑ‚Ð¸Ñ.")
        @Size(max = 500) String prophylacticAddress,

        @Schema(description = "ÐšÐ¾Ð»Ð¸Ñ‡ÐµÑÑ‚Ð²Ð¾ Ð¾Ð±ÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð½Ñ‹Ñ… ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ñ‹Ñ… Ð»Ð¸Ñ†.")
        @Size(max = 32) String contactCount,

        @Schema(description = "Ð¢Ñ€ÐµÐ±ÑƒÐµÐ¼Ð¾Ðµ Ð¿Ñ€Ð¾Ñ„Ð¸Ð»Ð°ÐºÑ‚Ð¸Ñ‡ÐµÑÐºÐ¾Ðµ ÑÑ€ÐµÐ´ÑÑ‚Ð²Ð¾.")
        @Size(max = 500) String requiredProphylacticSubstance,

        @Schema(description = "ÐŸÑ€Ð¾Ñ„Ð¸Ð»Ð°ÐºÑ‚Ð¸Ñ‡ÐµÑÐºÐ¾Ðµ ÑÑ€ÐµÐ´ÑÑ‚Ð²Ð¾, ÐºÐ¾Ñ‚Ð¾Ñ€Ñ‹Ð¼ Ñ„Ð°ÐºÑ‚Ð¸Ñ‡ÐµÑÐºÐ¸ Ð¿Ñ€Ð¾Ð²ÐµÐ´ÐµÐ½Ð° Ð¾Ð±Ñ€Ð°Ð±Ð¾Ñ‚ÐºÐ°.")
        @Size(max = 500) String treatedWithProphylacticSubstance,

        @Schema(description = "ÐŸÑ€Ð¾Ð²ÐµÐ´Ñ‘Ð½Ð½Ð¾Ðµ Ð»Ð°Ð±Ð¾Ñ€Ð°Ñ‚Ð¾Ñ€Ð½Ð¾Ðµ Ð¸ÑÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð¸Ðµ Ð³Ñ€ÑƒÐ¿Ð¿Ñ‹.")
        @Size(max = 500) String laboratoryTestConducted
) implements ChildRequest {
}
