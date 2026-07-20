package uz.uzinfocom.app.modules.form058.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения о месте работы/учёбы пациента для печатной формы №058.")
public record Form058PdfWorkplaceResponse(
        @Schema(description = "Наименование места работы/учёбы.")
        String organizationName,

        @Schema(description = "Наименование региона места работы/учёбы.")
        String regionName,

        @Schema(description = "Наименование района/города места работы/учёбы.")
        String districtName,

        @Schema(description = "Адрес места работы/учёбы.")
        String address
) {
}
