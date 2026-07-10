package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Ð¤Ð°ÐºÑ‚Ð¾Ñ€ Ñ€Ð¸ÑÐºÐ°, ÑÐ²ÑÐ·Ð°Ð½Ð½Ñ‹Ð¹ ÑÐ¾ ÑÐ»ÑƒÑ‡Ð°ÐµÐ¼ Ð·Ð°Ð±Ð¾Ð»ÐµÐ²Ð°Ð½Ð¸Ñ.")
public record Card161RiskFactorRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "ÐšÐ¾Ð´ Ñ„Ð°ÐºÑ‚Ð¾Ñ€Ð° Ñ€Ð¸ÑÐºÐ° (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String riskFactorCode,

        @Schema(description = "ÐÐ´Ñ€ÐµÑ/Ð¼ÐµÑÑ‚Ð¾Ð¿Ð¾Ð»Ð¾Ð¶ÐµÐ½Ð¸Ðµ, ÑÐ²ÑÐ·Ð°Ð½Ð½Ð¾Ðµ Ñ Ñ„Ð°ÐºÑ‚Ð¾Ñ€Ð¾Ð¼ Ñ€Ð¸ÑÐºÐ°.")
        @Size(max = 500) String addressLocation,

        @Schema(description = "Ð¡ÐµÐ·Ð¾Ð½/Ð¿ÐµÑ€Ð¸Ð¾Ð´ Ð¿Ñ€Ð¾ÑÐ²Ð»ÐµÐ½Ð¸Ñ Ñ„Ð°ÐºÑ‚Ð¾Ñ€Ð° Ñ€Ð¸ÑÐºÐ°.")
        @Size(max = 100) String seasonTime
) implements ChildRequest {
}
