package uz.uzinfocom.app.integration.api2.citizen.web;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenAddressType;

@Schema(description = "Адрес физического лица, полученный из API2 (v3/citizenAddress) и приведённый к справочникам региона/района приложения.")
public record CitizenAddressResponse(
        @Schema(description = "Тип регистрации.")
        CitizenAddressType type,

        @Schema(description = "Код региона в справочнике приложения, найденный по СОАТО-коду, полученному от API2.", example = "UZ-TK")
        String regionCode,

        @Schema(description = "Наименование региона на языке текущей локали.")
        String regionName,

        @Schema(description = "Код района в справочнике приложения, найденный по СОАТО-коду, полученному от API2.", example = "TK-294")
        String districtCode,

        @Schema(description = "Наименование района на языке текущей локали.")
        String districtName,

        @Schema(description = "ИНН (TIN) махалли в справочнике приложения, найденный по коду реестра uzcad (Guid), полученному от API2 — не внутренний code приложения (вида \"TK-294003\"), а именно TIN. Может быть null — API2 не передаёт для махалли ни СОАТО-код, ни сам ИНН напрямую (только этот более мягкий, не гарантированно уникальный ключ сопоставления), поэтому сопоставление не всегда возможно.", example = "202853324")
        String neighborhoodCode,

        @Schema(description = "Наименование махалли: из справочника приложения, если сопоставление по коду реестра uzcad удалось, иначе — в исходном виде, как его вернул API2.")
        String neighborhoodName,

        @Schema(description = "Адресная строка (улица, дом, квартира), как её вернул API2.")
        String streetAddress,

        @Schema(description = "Кадастровый номер объекта, как его вернул API2.")
        String cadastre,

        @Schema(description = "Дата регистрации по данному адресу, как её вернул API2.")
        String registrationDate
) {
}
