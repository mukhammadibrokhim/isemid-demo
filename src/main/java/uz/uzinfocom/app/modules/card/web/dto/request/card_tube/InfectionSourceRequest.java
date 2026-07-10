package uz.uzinfocom.app.modules.card.web.dto.request.card_tube;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Ð’Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ñ‹Ð¹ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸Ðº Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ Ñ‚ÑƒÐ±ÐµÑ€ÐºÑƒÐ»Ñ‘Ð·Ð½Ñ‹Ð¼ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð¾Ð¼.")
public record InfectionSourceRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "ÐšÐ¾Ð´ Ñ‚ÑƒÐ±ÐµÑ€ÐºÑƒÐ»Ñ‘Ð·Ð½Ð¾Ð³Ð¾ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð° (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String tbContactCode,

        @Schema(description = "Ð¤Ð˜Ðž Ð¿Ñ€ÐµÐ´Ð¿Ð¾Ð»Ð°Ð³Ð°ÐµÐ¼Ð¾Ð³Ð¾ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ° Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ.")
        @Size(max = 255) String fullName,

        @Schema(description = "ÐšÐ¾Ð´ ÑÑ‚ÐµÐ¿ÐµÐ½Ð¸ Ñ€Ð¾Ð´ÑÑ‚Ð²Ð° Ñ Ð¿Ñ€ÐµÐ´Ð¿Ð¾Ð»Ð°Ð³Ð°ÐµÐ¼Ñ‹Ð¼ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ¾Ð¼ Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String relationDegreeCode,

        @Schema(description = "Ð”Ð»Ð¸Ñ‚ÐµÐ»ÑŒÐ½Ð¾ÑÑ‚ÑŒ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð° Ñ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ¾Ð¼ Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ.")
        @Size(max = 255) String contactDuration
) implements ChildRequest {
}
