package uz.uzinfocom.app.modules.form0581.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Адрес по схеме региона/района/махалли/улицы/дома/квартиры для печатной формы №058-1 -
        используется как для владельца животного, так и для каждого из иных пострадавших лиц.
        """)
public record Form0581PdfLocationResponse(
        @Schema(description = "Наименование региона.")
        String regionName,

        @Schema(description = "Наименование района/города.")
        String districtName,

        @Schema(description = "Наименование массива/махалли.")
        String neighborhoodName,

        @Schema(description = "Улица.")
        String street,

        @Schema(description = "Номер дома.")
        String houseNumber,

        @Schema(description = "Номер квартиры.")
        String apartmentNumber
) {
}
