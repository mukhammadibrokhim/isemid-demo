package uz.uzinfocom.app.modules.card.web.dto.request.card205;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDateTime;

@Schema(description = "Ð¡Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¾ Ð´Ñ€ÑƒÐ³Ð¾Ð¼ Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ð¾Ð¼, Ð¿Ð¾ÑÑ‚Ñ€Ð°Ð´Ð°Ð²ÑˆÐµÐ¼ Ð¾Ñ‚ Ñ‚Ð¾Ð³Ð¾ Ð¶Ðµ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ° Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ.")
public record InformationOtherBittenAnimalsRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "ÐšÐ¾Ð´ ÐºÐ°Ñ‚ÐµÐ³Ð¾Ñ€Ð¸Ð¸ ÑƒÐºÑƒÑˆÐµÐ½Ð½Ð¾Ð³Ð¾ Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ð¾Ð³Ð¾ (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String bittenAnimalCategoryCode,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ð¸ Ð²Ñ€ÐµÐ¼Ñ ÑƒÐºÑƒÑÐ°.")
        LocalDateTime bittenDateTime,

        @Schema(description = "ÐœÐµÑÑ‚Ð¾, Ð³Ð´Ðµ Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ð¾Ðµ Ð±Ñ‹Ð»Ð¾ ÑƒÐºÑƒÑˆÐµÐ½Ð¾.")
        @Size(max = 500) String whereAnimalBitten
) implements ChildRequest {
}
