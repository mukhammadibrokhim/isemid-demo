package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.time.LocalDate;

@Schema(description = "Ð ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚ Ð»Ð°Ð±Ð¾Ñ€Ð°Ñ‚Ð¾Ñ€Ð½Ð¾Ð³Ð¾ Ð¸ÑÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð¸Ñ Ð¾Ð±ÑŠÐµÐºÑ‚Ð° Ð¾ÐºÑ€ÑƒÐ¶Ð°ÑŽÑ‰ÐµÐ¹ ÑÑ€ÐµÐ´Ñ‹.")
public record EnvironmentalLabTestRequest(
        @Schema(description = "Ð˜Ð´ÐµÐ½Ñ‚Ð¸Ñ„Ð¸ÐºÐ°Ñ‚Ð¾Ñ€ Ð·Ð°Ð¿Ð¸ÑÐ¸. ÐÐµ ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ (null) Ð¿Ñ€Ð¸ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¸Ð¸ Ð½Ð¾Ð²Ð¾Ð¹ Ð·Ð°Ð¿Ð¸ÑÐ¸; "
                + "ÑƒÐºÐ°Ð·Ñ‹Ð²Ð°ÐµÑ‚ÑÑ Ð´Ð»Ñ Ð¾Ð±Ð½Ð¾Ð²Ð»ÐµÐ½Ð¸Ñ ÑƒÐ¶Ðµ ÑÑƒÑ‰ÐµÑÑ‚Ð²ÑƒÑŽÑ‰ÐµÐ¹.")
        Long id,

        @Schema(description = "Ð”Ð°Ñ‚Ð° Ð¿Ñ€Ð¾Ð²ÐµÐ´ÐµÐ½Ð¸Ñ Ð¸ÑÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð¸Ñ.")
        LocalDate examinationDate,

        @Schema(description = "ÐžÐ±ÑÐ»ÐµÐ´ÑƒÐµÐ¼Ñ‹Ð¹ Ð¾Ð±ÑŠÐµÐºÑ‚ (Ñ‡Ð»ÐµÐ½Ð¸ÑÑ‚Ð¾Ð½Ð¾Ð³Ð¸Ðµ/Ð¶Ð¸Ð²Ð¾Ñ‚Ð½Ñ‹Ðµ).")
        @Size(max = 500) String objectArthropodsAnimals,

        @Schema(description = "ÐœÐ°Ñ‚ÐµÑ€Ð¸Ð°Ð», Ð²Ð·ÑÑ‚Ñ‹Ð¹ Ð´Ð»Ñ Ð¸ÑÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð¸Ñ.")
        @Size(max = 255) String material,

        @Schema(description = "ÐšÐ¾Ð»Ð¸Ñ‡ÐµÑÑ‚Ð²Ð¾ Ð¾Ñ‚Ð¾Ð±Ñ€Ð°Ð½Ð½Ñ‹Ñ… Ð¿Ñ€Ð¾Ð±.")
        @Size(max = 100) String sampleQuantity,

        @Schema(description = "Ð’Ð¸Ð´ Ð¸ÑÑÐ»ÐµÐ´Ð¾Ð²Ð°Ð½Ð¸Ñ Ð¸ ÐµÐ³Ð¾ Ñ€ÐµÐ·ÑƒÐ»ÑŒÑ‚Ð°Ñ‚.")
        @Size(max = 500) String testTypeAndResult
) implements ChildRequest {
}
