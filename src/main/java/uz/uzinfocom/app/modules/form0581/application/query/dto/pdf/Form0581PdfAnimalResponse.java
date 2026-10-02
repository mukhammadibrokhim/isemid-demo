package uz.uzinfocom.app.modules.form0581.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения о животном, нанёсшем укус/царапину/ослюнение, для печатной формы №058-1.")
public record Form0581PdfAnimalResponse(
        @Schema(description = "Наименование категории животного.")
        String animalCategoryName,

        @Schema(description = "Окрас животного.")
        String animalColor,

        @Schema(description = "Вид животного.")
        String animalType,

        @Schema(description = "Порода животного.")
        String animalBreed
) {
}
