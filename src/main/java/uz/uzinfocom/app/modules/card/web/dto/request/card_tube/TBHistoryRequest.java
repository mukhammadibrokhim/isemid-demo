package uz.uzinfocom.app.modules.card.web.dto.request.card_tube;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDate;

@Schema(description = "Ð¡Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¾ Ñ€Ð°Ð½ÐµÐµ Ð¿ÐµÑ€ÐµÐ½ÐµÑÑ‘Ð½Ð½Ð¾Ð¼ ÑÐ»ÑƒÑ‡Ð°Ðµ Ñ‚ÑƒÐ±ÐµÑ€ÐºÑƒÐ»Ñ‘Ð·Ð°.")
public record TBHistoryRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "ÐœÐµÑÑ‚Ð¾, Ð³Ð´Ðµ Ð¿Ñ€Ð¾Ð¸Ð·Ð¾ÑˆÐ»Ð¾ Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ðµ.")
        @Size(max = 255) String infectionLocation,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ.")
        LocalDate infectionDate,

        @Schema(description = "ÐšÐ¾Ð´ Ð´Ð¸Ð°Ð³Ð½Ð¾Ð·Ð° Ð¿Ð¾ ÐœÐšÐ‘-10.")
        @Size(max = 64) String mkb10Code,

        @Schema(description = "ÐÐ°Ð¸Ð¼ÐµÐ½Ð¾Ð²Ð°Ð½Ð¸Ðµ Ð´Ð¸Ð°Ð³Ð½Ð¾Ð·Ð° Ð¿Ð¾ ÐœÐšÐ‘-10.")
        @Size(max = 500) String mkb10Name,

        @Schema(description = "Ð“Ñ€ÑƒÐ¿Ð¿Ð° Ð´Ð¸ÑÐ¿Ð°Ð½ÑÐµÑ€Ð½Ð¾Ð³Ð¾ ÑƒÑ‡Ñ‘Ñ‚Ð° Ð¿Ñ€Ð¸ Ñ€ÐµÐ³Ð¸ÑÑ‚Ñ€Ð°Ñ†Ð¸Ð¸ ÑÐ»ÑƒÑ‡Ð°Ñ.")
        @Size(max = 255) String registrationGroup
) implements ChildRequest {
}
