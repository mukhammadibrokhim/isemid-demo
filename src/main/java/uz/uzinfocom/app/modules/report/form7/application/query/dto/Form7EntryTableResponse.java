package uz.uzinfocom.app.modules.report.form7.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Строка ручной записи Shakl №7 для постраничного табличного ответа.")
public record Form7EntryTableResponse(
        @Schema(description = "Внутренний идентификатор записи.", example = "1")
        Long id,

        @Schema(description = "Внутренний идентификатор организации, создавшей запись.", example = "10")
        Long organizationId,

        @Schema(description = "Наименование организации, создавшей запись.", example = "Toshkent shahar SES")
        String organizationName,

        @Schema(description = "Код региона (худуд) организации, создавшей запись.", example = "UZ-TK")
        String regionCode,

        @Schema(description = "Наименование региона (худуд) организации, создавшей запись.", example = "Toshkent shahri")
        String regionName,

        @Schema(description = "Код района организации, создавшей запись.", example = "UZ-TK-01")
        String districtCode,

        @Schema(description = "Наименование района организации, создавшей запись.", example = "Chilonzor tumani")
        String districtName,

        @Schema(description = "Начало периода отчёта.", example = "2026-01-01")
        LocalDate fromDate,

        @Schema(description = "Конец периода отчёта.", example = "2026-01-31")
        LocalDate toDate,

        @Schema(description = "Davr boshida kasallanishlar soni.", example = "5")
        Integer casesAtPeriodStart,

        @Schema(description = "Hisobot davrida ro'yxatga olingan bemorlar — Jami.", example = "42")
        Long registeredTotal,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — 14 yoshgacha bolalar.", example = "10")
        Long registeredUnder14,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — 18 yoshgacha bolalar.", example = "14")
        Long registeredUnder18,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — kattalar (18+).", example = "28")
        Long registeredAdult,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — ayollar.", example = "20")
        Long registeredFemale,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — shahar aholisi.", example = "25")
        Integer registeredUrbanCount,

        @Schema(description = "Hisobot davrida ro'yxatga olingan — qishloq aholisi.", example = "17")
        Integer registeredRuralCount,

        @Schema(description = "Ulardan — tekshiruvdan o'tdi.", example = "30")
        Integer examinedCount,

        @Schema(description = "Ulardan — tekshirilishi kerak.", example = "12")
        Integer toBeExaminedCount,

        @Schema(description = "Ulardan — birlamchi tashxis tasdiqlandi.", example = "31")
        Long primaryDiagnosisConfirmed,

        @Schema(description = "Ulardan — shifoxonaga yotqizilgan.", example = "8")
        Integer hospitalizedCount,

        @Schema(description = "Davr ohirida kasallanishlar soni.", example = "9")
        Integer casesAtPeriodEnd,

        @Schema(description = "Kasallanishning o'sish/kamayish (+/-) — casesAtPeriodEnd − casesAtPeriodStart.", example = "4")
        Integer caseChange,

        @Schema(description = "Дата создания записи.")
        Instant createdAt
) {
}
