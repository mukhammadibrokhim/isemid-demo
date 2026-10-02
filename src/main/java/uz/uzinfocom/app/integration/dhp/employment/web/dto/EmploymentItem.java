package uz.uzinfocom.app.integration.dhp.employment.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;

@Schema(description = "Одно место работы гражданина.")
public record EmploymentItem(
        @Schema(description = "Наименование организации-работодателя.")
        String organizationName,

        @Schema(description = "ИНН (STIR) организации-работодателя.")
        String organizationTin,

        @Schema(description = "Должность / профессия.")
        String position,

        @Schema(description = "Дата приёма на работу.")
        LocalDate startDate,

        @Schema(description = "Дата увольнения; пусто, если гражданин работает в настоящее время.")
        LocalDate endDate,

        @Schema(description = "Признак действующего места работы.")
        boolean current,

        @Schema(description = "Исходная запись DHP без изменений. Временное поле: остаётся, пока состав "
                + "полей ответа egov MOL не подтверждён на реальных данных.")
        JsonNode raw
) {
}
