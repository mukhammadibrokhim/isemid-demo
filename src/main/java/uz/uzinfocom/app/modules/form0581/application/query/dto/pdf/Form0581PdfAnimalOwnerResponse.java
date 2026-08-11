package uz.uzinfocom.app.modules.form0581.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения о владельце животного для печатной формы №058-1.")
public record Form0581PdfAnimalOwnerResponse(
        @Schema(description = "ФИО владельца животного одной строкой.")
        String fullName,

        @Schema(description = "Адрес проживания владельца животного.")
        Form0581PdfLocationResponse address
) {
}
