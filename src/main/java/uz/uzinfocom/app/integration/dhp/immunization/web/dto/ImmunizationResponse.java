package uz.uzinfocom.app.integration.dhp.immunization.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Сведения об иммунизации гражданина, полученные из DHP FHIR (Immunization) по ПИНФЛ.")
public record ImmunizationResponse(
        @Schema(description = "ПИНФЛ (NNUZB), по которому выполнен поиск.")
        String nnuzb,

        @Schema(description = "Записи о вакцинации, новые первыми. Пусто, если сведений нет.")
        List<ImmunizationItem> items
) {
}
