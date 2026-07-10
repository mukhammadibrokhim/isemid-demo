package uz.uzinfocom.app.modules.card.web.dto.request.card205;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

@Schema(description = "Ð¡Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¾ Ð²Ð»Ð°Ð´ÐµÐ»ÑŒÑ†Ðµ Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ð¾Ð³Ð¾, ÑƒÐºÑƒÑˆÐµÐ½Ð½Ð¾Ð³Ð¾ Ñ‚ÐµÐ¼ Ð¶Ðµ Ð¸ÑÑ‚Ð¾Ñ‡Ð½Ð¸ÐºÐ¾Ð¼ Ð·Ð°Ñ€Ð°Ð¶ÐµÐ½Ð¸Ñ.")
public record InformationAboutAnimaBittenPeopleRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "ÐšÐ¾Ð´ ÐºÐ°Ñ‚ÐµÐ³Ð¾Ñ€Ð¸Ð¸ Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ð¾Ð³Ð¾ (Ð¿Ð¾ ÑÐ¿Ñ€Ð°Ð²Ð¾Ñ‡Ð½Ð¸ÐºÑƒ).")
        @Size(max = 64) String animalCategoryCode,

        @Schema(description = "Ð’Ð¸Ð´ Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ð¾Ð³Ð¾.")
        @Size(max = 255) String animalType,

        @Schema(description = "Ð¤Ð˜Ðž Ð²Ð»Ð°Ð´ÐµÐ»ÑŒÑ†Ð° ÑƒÐºÑƒÑˆÐµÐ½Ð½Ð¾Ð³Ð¾ Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ð¾Ð³Ð¾.")
        @Size(max = 255) String fullNameOfAnimalBittenOwner,

        @Schema(description = "ÐÐ´Ñ€ÐµÑ Ð²Ð»Ð°Ð´ÐµÐ»ÑŒÑ†Ð° ÑƒÐºÑƒÑˆÐµÐ½Ð½Ð¾Ð³Ð¾ Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ð¾Ð³Ð¾.")
        @Size(max = 500) String addressOfAnimalBittenOwner,

        @Schema(description = "ÐšÐ¾Ð´ Ñ€ÐµÐ³Ð¸Ð¾Ð½Ð° Ð¿Ð¾ ÐºÐ»Ð°ÑÑÐ¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€Ñƒ Ð°Ð´Ð¼Ð¸Ð½Ð¸ÑÑ‚Ñ€Ð°Ñ‚Ð¸Ð²Ð½Ð¾-Ñ‚ÐµÑ€Ñ€Ð¸Ñ‚Ð¾Ñ€Ð¸Ð°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð´ÐµÐ»ÐµÐ½Ð¸Ñ.")
        @Size(max = 64) String region,

        @Schema(description = "ÐšÐ¾Ð´ Ñ€Ð°Ð¹Ð¾Ð½Ð° Ð¿Ð¾ ÐºÐ»Ð°ÑÑÐ¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€Ñƒ Ð°Ð´Ð¼Ð¸Ð½Ð¸ÑÑ‚Ñ€Ð°Ñ‚Ð¸Ð²Ð½Ð¾-Ñ‚ÐµÑ€Ñ€Ð¸Ñ‚Ð¾Ñ€Ð¸Ð°Ð»ÑŒÐ½Ð¾Ð³Ð¾ Ð´ÐµÐ»ÐµÐ½Ð¸Ñ.")
        @Size(max = 64) String district,

        @Schema(description = "ÐœÐ°ÑÑÐ¸Ð²/Ð¼Ð°Ñ…Ð°Ð»Ð»Ñ Ð¿Ñ€Ð¾Ð¶Ð¸Ð²Ð°Ð½Ð¸Ñ Ð²Ð»Ð°Ð´ÐµÐ»ÑŒÑ†Ð°.")
        @Size(max = 255) String neighborhood,

        @Schema(description = "Ð£Ð»Ð¸Ñ†Ð° Ð¿Ñ€Ð¾Ð¶Ð¸Ð²Ð°Ð½Ð¸Ñ Ð²Ð»Ð°Ð´ÐµÐ»ÑŒÑ†Ð°.")
        @Size(max = 255) String street,

        @Schema(description = "ÐÐ¾Ð¼ÐµÑ€ Ð´Ð¾Ð¼Ð°.")
        @Size(max = 32) String houseNumber,

        @Schema(description = "ÐÐ¾Ð¼ÐµÑ€ ÐºÐ²Ð°Ñ€Ñ‚Ð¸Ñ€Ñ‹.")
        @Size(max = 32) String apartmentNumber
) implements ChildRequest {
}
