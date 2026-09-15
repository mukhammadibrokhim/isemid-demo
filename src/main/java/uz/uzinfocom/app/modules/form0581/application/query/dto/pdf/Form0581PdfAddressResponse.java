package uz.uzinfocom.app.modules.form0581.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Адрес пациента для печатной формы №058-1 (постоянный или текущий).")
public record Form0581PdfAddressResponse(
        @Schema(description = "Наименование региона.")
        String regionName,

        @Schema(description = "Наименование района/города.")
        String districtName,

        @Schema(description = "Наименование массива/махалли.")
        String neighborhoodName,

        @Schema(description = "Улица.")
        String streetAddress,

        @Schema(description = "Номер дома.")
        String houseNumber,

        @Schema(description = "Номер квартиры.")
        String apartmentNumber
) {
}
