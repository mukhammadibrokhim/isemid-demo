package uz.uzinfocom.app.modules.card.web.dto.request.card205;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDateTime;

@Schema(description = "Ð¡Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¾ Ð´Ñ€ÑƒÐ³Ð¾Ð¼ Ð»Ð¸Ñ†Ðµ, Ð¿Ð¾ÑÑ‚Ñ€Ð°Ð´Ð°Ð²ÑˆÐµÐ¼ Ð¾Ñ‚ ÑƒÐºÑƒÑÐ° Ñ‚Ð¾Ð³Ð¾ Ð¶Ðµ Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ð¾Ð³Ð¾.")
public record InformationOtherBittenPeopleRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "Ð¤Ð°Ð¼Ð¸Ð»Ð¸Ñ Ð¿Ð¾ÑÑ‚Ñ€Ð°Ð´Ð°Ð²ÑˆÐµÐ³Ð¾.")
        @Size(max = 255) String lastName,

        @Schema(description = "Ð˜Ð¼Ñ Ð¿Ð¾ÑÑ‚Ñ€Ð°Ð´Ð°Ð²ÑˆÐµÐ³Ð¾.")
        @Size(max = 255) String firstName,

        @Schema(description = "ÐžÑ‚Ñ‡ÐµÑÑ‚Ð²Ð¾ Ð¿Ð¾ÑÑ‚Ñ€Ð°Ð´Ð°Ð²ÑˆÐµÐ³Ð¾.")
        @Size(max = 255) String middleName,

        @Schema(description = "ÐŸÐ¾Ð» Ð¿Ð¾ÑÑ‚Ñ€Ð°Ð´Ð°Ð²ÑˆÐµÐ³Ð¾.")
        @Size(max = 32) String gender,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ñ€Ð¾Ð¶Ð´ÐµÐ½Ð¸Ñ Ð¿Ð¾ÑÑ‚Ñ€Ð°Ð´Ð°Ð²ÑˆÐµÐ³Ð¾.")
        @Size(max = 32) String birthDate,

        @Schema(description = "ÐÐ´Ñ€ÐµÑ Ð¿Ñ€Ð¾Ð¶Ð¸Ð²Ð°Ð½Ð¸Ñ Ð¿Ð¾ÑÑ‚Ñ€Ð°Ð´Ð°Ð²ÑˆÐµÐ³Ð¾.")
        @Size(max = 500) String livingAddress,

        @Schema(description = "ÐšÐ¾Ð´ Ñ€ÐµÐ³Ð¸Ð¾Ð½Ð° Ð¿Ð¾ ÐºÐ»Ð°ÑÑÐ¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€Ñƒ Ð°Ð´Ð¼Ð¸Ð½Ð¸ÑÑ‚Ñ€Ð°Ñ‚Ð¸Ð²Ð½Ð¾-Ñ‚ÐµÑ€Ñ€Ð¸Ñ‚Ð¾Ñ€Ð¸Ð°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð´ÐµÐ»ÐµÐ½Ð¸Ñ.")
        @Size(max = 64) String region,

        @Schema(description = "ÐšÐ¾Ð´ Ñ€Ð°Ð¹Ð¾Ð½Ð° Ð¿Ð¾ ÐºÐ»Ð°ÑÑÐ¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€Ñƒ Ð°Ð´Ð¼Ð¸Ð½Ð¸ÑÑ‚Ñ€Ð°Ñ‚Ð¸Ð²Ð½Ð¾-Ñ‚ÐµÑ€Ñ€Ð¸Ñ‚Ð¾Ñ€Ð¸Ð°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð´ÐµÐ»ÐµÐ½Ð¸Ñ.")
        @Size(max = 64) String district,

        @Schema(description = "ÐœÐ°ÑÑÐ¸Ð²/Ð¼Ð°Ñ…Ð°Ð»Ð»Ñ Ð¿Ñ€Ð¾Ð¶Ð¸Ð²Ð°Ð½Ð¸Ñ.")
        @Size(max = 255) String neighborhood,

        @Schema(description = "Ð£Ð»Ð¸Ñ†Ð° Ð¿Ñ€Ð¾Ð¶Ð¸Ð²Ð°Ð½Ð¸Ñ.")
        @Size(max = 255) String street,

        @Schema(description = "ÐÐ¾Ð¼ÐµÑ€ Ð´Ð¾Ð¼Ð°.")
        @Size(max = 32) String houseNumber,

        @Schema(description = "ÐÐ¾Ð¼ÐµÑ€ ÐºÐ²Ð°Ñ€Ñ‚Ð¸Ñ€Ñ‹.")
        @Size(max = 32) String apartmentNumber,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ð¸ Ð²Ñ€ÐµÐ¼Ñ ÑƒÐºÑƒÑÐ°.")
        LocalDateTime bittenDate
) implements ChildRequest {
}
