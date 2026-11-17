package uz.uzinfocom.app.integration.api2.legalentity.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения о юридическом лице: только наименование и адреса (без руководителей и прочих данных API2).")
public record LegalEntityLookupData(
        @Schema(description = "ИНН (STIR) юридического лица.")
        String tin,

        @Schema(description = "Наименование юридического лица.")
        String name,

        @Schema(description = "Краткое наименование, если API2 его вернул.")
        String shortName,

        @Schema(description = "Юридический адрес.")
        String legalAddress,

        @Schema(description = "Фактический адрес, если API2 его вернул.")
        String actualAddress
) {

    public boolean isEmpty() {
        return name == null && shortName == null && legalAddress == null && actualAddress == null;
    }
}
