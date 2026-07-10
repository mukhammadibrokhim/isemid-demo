package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Ð’Ð¾Ð·Ð¼Ð¾Ð¶Ð½Ñ‹Ð¹ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸Ðº Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ (Ð»Ð¸Ñ†Ð¾/Ð´Ð¾Ð½Ð¾Ñ€).")
public record InfectionSourceRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "Ð¤Ð˜Ðž Ð¿Ñ€ÐµÐ´Ð¿Ð¾Ð»Ð°Ð³Ð°ÐµÐ¼Ð¾Ð³Ð¾ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ° Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ.")
        @Size(max = 500) String fullName,

        @Schema(description = "ÐšÐ»Ð¸Ð½Ð¸Ñ‡ÐµÑÐºÐ°Ñ Ñ„Ð¾Ñ€Ð¼Ð° Ð·Ð°Ð±Ð¾Ð»ÐµÐ²Ð°Ð½Ð¸Ñ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ° Ð»Ð¸Ð±Ð¾ ÑÑ‚Ð°Ñ‚ÑƒÑ Ð´Ð¾Ð½Ð¾Ñ€Ð°.")
        @Size(max = 500) String diagnosisClinicalFormOrDonorStatus,

        @Schema(description = "ÐšÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ñ‹Ðµ Ð´Ð°Ð½Ð½Ñ‹Ðµ Ð¸ Ð¼ÐµÑÑ‚Ð¾ Ð¶Ð¸Ñ‚ÐµÐ»ÑŒÑÑ‚Ð²Ð° Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ°/Ð´Ð¾Ð½Ð¾Ñ€Ð°.")
        @Size(max = 500) String contactInfoAndDonorResidence,

        @Schema(description = "Ð ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚ Ð»Ð°Ð±Ð¾Ñ€Ð°Ñ‚Ð¾Ñ€Ð½Ð¾Ð³Ð¾ Ð¸ÑÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð¸Ñ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ° Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ.")
        @Size(max = 500) String testResult
) implements ChildRequest {
}
