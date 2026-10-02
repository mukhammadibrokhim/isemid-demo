package uz.uzinfocom.app.modules.report.form2.manual.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Автоматически вычисляемые поля для формы создания Shakl №2 " +
        "(область видимости — текущая организация вызывающего).")
public record Form2ManualEntryPrefillResponse(
        @Schema(description = "Начало периода отчёта.", example = "2026-01-01")
        LocalDate from,

        @Schema(description = "Конец периода отчёта.", example = "2026-01-31")
        LocalDate to,

        @Schema(description = "Внутренний идентификатор организации вызывающего.", example = "10")
        Long organizationId,

        @Schema(description = "Наименование организации вызывающего.", example = "Toshkent shahar SES")
        String organizationName,

        @Schema(description = "Код региона (худуд) организации вызывающего.", example = "UZ-TK")
        String regionCode,

        @Schema(description = "Наименование региона (худуд) организации вызывающего.", example = "Toshkent shahri")
        String regionName,

        @Schema(description = "Код района организации вызывающего.", example = "UZ-TK-01")
        String districtCode,

        @Schema(description = "Наименование района организации вызывающего.", example = "Chilonzor tumani")
        String districtName,

        @Schema(description = "O'tgan yilda qayd etilgan holatlar soni (тот же период год назад).", example = "12")
        long registeredCasesLastYear,

        @Schema(description = "Joriy yilda qayd etilgan holatlar soni (выбранный период).", example = "15")
        long registeredCasesCurrentYear
) {
}
