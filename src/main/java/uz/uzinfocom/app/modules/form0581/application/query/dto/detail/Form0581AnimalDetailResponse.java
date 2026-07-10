package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения о животном, нанёсшем укус/царапину/ослюнение.")
public record Form0581AnimalDetailResponse(
        @Schema(description = "Код категории животного (по справочнику).")
        String animalCategoryCode,

        @Schema(description = "Окрас животного.")
        String animalColor,

        @Schema(description = "Вид животного.")
        String animalType,

        @Schema(description = "Порода животного.")
        String animalBreed
) {
}
