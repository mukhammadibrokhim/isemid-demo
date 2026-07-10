package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDateTime;

@Schema(description = "ÐžÐ±ÑŠÐµÐºÑ‚ Ð¾ÐºÑ€ÑƒÐ¶Ð°ÑŽÑ‰ÐµÐ¹ ÑÑ€ÐµÐ´Ñ‹ (Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸Ðº Ð²Ð¾Ð´Ñ‹/Ð¿Ð¸Ñ‰Ð¸), Ð¿Ð¾Ð´Ð»ÐµÐ¶Ð°Ñ‰Ð¸Ð¹ Ð¾Ð±ÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð¸ÑŽ.")
public record EnvironmentalSourceRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "Ð’Ð¸Ð´Ñ‹ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ¾Ð² Ð¿Ð¸Ñ‚Ð°Ð½Ð¸Ñ Ð¸ Ð²Ð¾Ð´Ñ‹.")
        @Size(max = 500) String foodAndWaterSourceTypes,

        @Schema(description = "ÐœÐµÑÑ‚Ð¾ Ð¾Ñ‚Ð±Ð¾Ñ€Ð° Ð¿Ñ€Ð¾Ð±Ñ‹.")
        @Size(max = 500) String collectionLocation,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ð¸ Ð²Ñ€ÐµÐ¼Ñ Ð¾Ñ‚Ð±Ð¾Ñ€Ð° Ð¿Ñ€Ð¾Ð±Ñ‹.")
        LocalDateTime collectionTime,

        @Schema(description = "ÐœÐµÑÑ‚Ð¾ Ð¸ÑÐ¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ð½Ð¸Ñ Ð¿Ñ€Ð¾Ð´ÑƒÐºÑ‚Ð°/Ð²Ð¾Ð´Ñ‹.")
        @Size(max = 500) String usageLocation,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ð¸ Ð²Ñ€ÐµÐ¼Ñ Ð¸ÑÐ¿Ð¾Ð»ÑŒÐ·Ð¾Ð²Ð°Ð½Ð¸Ñ Ð¿Ñ€Ð¾Ð´ÑƒÐºÑ‚Ð°/Ð²Ð¾Ð´Ñ‹.")
        LocalDateTime usageTime,

        @Schema(description = "Ð£ÑÐ»Ð¾Ð²Ð¸Ñ Ñ…Ñ€Ð°Ð½ÐµÐ½Ð¸Ñ Ð¿Ñ€Ð¾Ð´ÑƒÐºÑ‚Ð°/Ð²Ð¾Ð´Ñ‹.")
        @Size(max = 500) String storageConditions,

        @Schema(description = "ÐžÑ‚Ð·Ñ‹Ð² Ð¾ ÐºÐ°Ñ‡ÐµÑÑ‚Ð²Ðµ Ð¿Ñ€Ð¾Ð´ÑƒÐºÑ‚Ð°/Ð²Ð¾Ð´Ñ‹ ÑÐ¾ ÑÐ»Ð¾Ð² Ð¿Ð°Ñ†Ð¸ÐµÐ½Ñ‚Ð° Ð¸ Ð´Ñ€ÑƒÐ³Ð¸Ñ… Ð»Ð¸Ñ†.")
        @Size(max = 1000) String qualityFeedbackFromPatientAndOthers
) implements ChildRequest {
}
