package uz.uzinfocom.app.modules.card.web.dto.request.card_tube;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDate;

@Schema(description = "Ð ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚ Ñ€ÐµÐ½Ñ‚Ð³ÐµÐ½Ð¾Ð³Ñ€Ð°Ñ„Ð¸Ð¸ Ð³Ñ€ÑƒÐ´Ð½Ð¾Ð¹ ÐºÐ»ÐµÑ‚ÐºÐ¸, Ð²Ñ‹Ð¿Ð¾Ð»Ð½ÐµÐ½Ð½Ð¾Ð¹ Ð´Ð¾ Ð²Ñ‹ÑÐ²Ð»ÐµÐ½Ð¸Ñ ÐœÐ‘Ð¢.")
public record XRayRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ð¿Ñ€Ð¾Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ñ€ÐµÐ½Ñ‚Ð³ÐµÐ½Ð¾Ð³Ñ€Ð°Ñ„Ð¸Ð¸.")
        LocalDate xrayDate,

        @Schema(description = "ÐœÐµÑÑ‚Ð¾ (ÑƒÑ‡Ñ€ÐµÐ¶Ð´ÐµÐ½Ð¸Ðµ) Ð¿Ñ€Ð¾Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ñ€ÐµÐ½Ñ‚Ð³ÐµÐ½Ð¾Ð³Ñ€Ð°Ñ„Ð¸Ð¸.")
        @Size(max = 255) String xrayPlace,

        @Schema(description = "Ð ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚ Ñ€ÐµÐ½Ñ‚Ð³ÐµÐ½Ð¾Ð³Ñ€Ð°Ñ„Ð¸Ð¸.")
        @Size(max = 500) String result
) implements ChildRequest {
}
