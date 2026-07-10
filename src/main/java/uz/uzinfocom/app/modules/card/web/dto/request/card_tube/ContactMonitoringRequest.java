package uz.uzinfocom.app.modules.card.web.dto.request.card_tube;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDate;

@Schema(description = "Ð¡Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¾ Ð½Ð°Ð±Ð»ÑŽÐ´ÐµÐ½Ð¸Ð¸ Ð·Ð° ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ñ‹Ð¼ Ð»Ð¸Ñ†Ð¾Ð¼.")
public record ContactMonitoringRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "Ð¤Ð˜Ðž ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        @Size(max = 255) String fullName,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ñ€Ð¾Ð¶Ð´ÐµÐ½Ð¸Ñ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        LocalDate birthDate,

        @Schema(description = "Ð’Ð¾Ð·Ñ€Ð°ÑÑ‚ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        Integer age,

        @Schema(description = "ÐšÐ¾Ð´ ÑÑ‚ÐµÐ¿ÐµÐ½Ð¸ Ñ€Ð¾Ð´ÑÑ‚Ð²Ð°/Ð¾Ñ‚Ð½Ð¾ÑˆÐµÐ½Ð¸Ñ Ñ Ð¿Ð°Ñ†Ð¸ÐµÐ½Ñ‚Ð¾Ð¼ (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String relationCode,

        @Schema(description = "ÐœÐµÑÑ‚Ð¾ Ñ€Ð°Ð±Ð¾Ñ‚Ñ‹ Ð¸Ð»Ð¸ ÑƒÑ‡Ñ‘Ð±Ñ‹ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð°.")
        @Size(max = 255) String workplaceOrStudyPlace,

        @Schema(description = "Ð£Ñ‡Ñ€ÐµÐ¶Ð´ÐµÐ½Ð¸Ðµ, Ð¿Ð¾Ð»ÑƒÑ‡Ð¸Ð²ÑˆÐµÐµ ÑƒÐ²ÐµÐ´Ð¾Ð¼Ð»ÐµÐ½Ð¸Ðµ Ð¾ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð¼ Ð»Ð¸Ñ†Ðµ.")
        @Size(max = 255) String notificationReceiver,

        @Schema(description = "Ð”Ð°Ñ‚Ð° ÑƒÑÑ‚Ð°Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ Ð´Ð¸Ð°Ð³Ð½Ð¾Ð·Ð° Ñƒ ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ð¾Ð³Ð¾ Ð»Ð¸Ñ†Ð° (Ð¿Ñ€Ð¸ Ð²Ñ‹ÑÐ²Ð»ÐµÐ½Ð¸Ð¸ Ð·Ð°Ð±Ð¾Ð»ÐµÐ²Ð°Ð½Ð¸Ñ).")
        LocalDate diagnosisDate,

        @Schema(description = "ÐšÐ¾Ð´ Ñ‚ÐµÐºÑƒÑ‰ÐµÐ³Ð¾ ÑÑ‚Ð°Ñ‚ÑƒÑÐ° Ð½Ð°Ð±Ð»ÑŽÐ´ÐµÐ½Ð¸Ñ Ð·Ð° ÐºÐ¾Ð½Ñ‚Ð°ÐºÑ‚Ð½Ñ‹Ð¼ Ð»Ð¸Ñ†Ð¾Ð¼ (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String contactStatusCode
) implements ChildRequest {
}
