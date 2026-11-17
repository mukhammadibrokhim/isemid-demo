package uz.uzinfocom.app.integration.dhp.employment.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Сведения о трудовой занятости гражданина, полученные из DHP (egov MOL) по ПИНФЛ.")
public record EmploymentResponse(
        @Schema(description = "ПИНФЛ (NNUZB), по которому выполнен поиск.")
        String nnuzb,

        @Schema(description = "Найденные места работы. Пусто, если сведений о занятости нет.")
        List<EmploymentItem> items
) {
}
