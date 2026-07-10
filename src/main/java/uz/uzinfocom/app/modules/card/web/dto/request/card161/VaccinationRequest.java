package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDateTime;

@Schema(description = "Ð¡Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¾ Ð²Ð°ÐºÑ†Ð¸Ð½Ð°Ñ†Ð¸Ð¸ Ð¿Ð°Ñ†Ð¸ÐµÐ½Ñ‚Ð°.")
public record VaccinationRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "ÐšÐ¾Ð´ Ð¿Ð¾Ð´Ñ‚Ð²ÐµÑ€Ð¶Ð´ÐµÐ½Ð¸Ñ Ñ„Ð°ÐºÑ‚Ð° Ð²Ð°ÐºÑ†Ð¸Ð½Ð°Ñ†Ð¸Ð¸ (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String vaccinationVerifiedCode,

        @Schema(description = "ÐÐ°Ð¸Ð¼ÐµÐ½Ð¾Ð²Ð°Ð½Ð¸Ðµ Ð²Ð°ÐºÑ†Ð¸Ð½Ñ‹.")
        @Size(max = 255) String vaccinationName,

        @Schema(description = "Ð¡ÐµÑ€Ð¸Ð¹Ð½Ñ‹Ð¹ Ð½Ð¾Ð¼ÐµÑ€ Ð¿Ñ€ÐµÐ¿Ð°Ñ€Ð°Ñ‚Ð°.")
        @Size(max = 100) String serialNumber,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ð¸ Ð²Ñ€ÐµÐ¼Ñ Ð¿Ñ€Ð¾Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð²Ð°ÐºÑ†Ð¸Ð½Ð°Ñ†Ð¸Ð¸.")
        LocalDateTime vaccinationDate,

        @Schema(description = "ÐžÐ±ÑŠÑ‘Ð¼ Ð²Ð²ÐµÐ´Ñ‘Ð½Ð½Ð¾Ð¹ Ð´Ð¾Ð·Ñ‹ Ð¿Ñ€ÐµÐ¿Ð°Ñ€Ð°Ñ‚Ð°.")
        Integer doseVolume,

        @Schema(description = "ÐŸÑ€Ð¸Ð·Ð½Ð°Ðº Ñ‚Ð¾Ð³Ð¾, Ñ‡Ñ‚Ð¾ Ð²Ð°ÐºÑ†Ð¸Ð½Ð°Ñ†Ð¸Ñ Ð¿Ñ€Ð¾Ð²ÐµÐ´ÐµÐ½Ð° Ð¿Ð¾ ÑƒÑÑ‚Ð°Ð½Ð¾Ð²Ð»ÐµÐ½Ð½Ð¾Ð¼Ñƒ Ð³Ñ€Ð°Ñ„Ð¸ÐºÑƒ.")
        Boolean scheduled
) implements ChildRequest {
}
