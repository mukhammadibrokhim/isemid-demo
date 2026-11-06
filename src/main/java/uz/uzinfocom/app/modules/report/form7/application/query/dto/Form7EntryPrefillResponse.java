package uz.uzinfocom.app.modules.report.form7.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Автоматически вычисляемые поля для формы создания Shakl №7 "
        + "(область видимости — текущая организация вызывающего). Блок «Hisobot davrida ro'yxatga "
        + "olingan bemorlar» и «Birlamchi tashxis tasdiqlandi» считаются по form058 + form058_1.")
public record Form7EntryPrefillResponse(
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

        @Schema(description = "Hisobot davrida ro'yxatga olingan bemorlar — Jami.", example = "42")
        long registeredTotal,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — 14 yoshgacha bolalar.", example = "10")
        long registeredUnder14,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — 18 yoshgacha bolalar.", example = "14")
        long registeredUnder18,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — kattalar (18+).", example = "28")
        long registeredAdult,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — ayollar.", example = "20")
        long registeredFemale,

        @Schema(description = "Ulardan — birlamchi tashxis tasdiqlandi (status = APPROVED).", example = "31")
        long primaryDiagnosisConfirmed
) {
}
