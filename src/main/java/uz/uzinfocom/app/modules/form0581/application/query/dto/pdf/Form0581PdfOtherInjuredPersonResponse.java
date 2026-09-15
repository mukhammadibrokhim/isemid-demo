package uz.uzinfocom.app.modules.form0581.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения об ином пострадавшем в том же происшествии для печатной формы №058-1.")
public record Form0581PdfOtherInjuredPersonResponse(
        @Schema(description = "ФИО пострадавшего одной строкой.")
        String fullName,

        @Schema(description = "Адрес проживания пострадавшего.")
        Form0581PdfLocationResponse address
) {
}
