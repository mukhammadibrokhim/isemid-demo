package uz.uzinfocom.app.modules.report.form31.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Строка ручной записи Shakl №3-1 для постраничного табличного ответа.")
public record Form31EntryTableResponse(
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

        @Schema(description = "Grippga o'xshash kasallik (ГПЗ) holatlari soni.", example = "0")
        Integer iliCasesCount,

        @Schema(description = "O'RI holatlari soni.", example = "0")
        Integer ariCasesCount,

        @Schema(description = "O'tkir zotiljam holatlari soni.", example = "0")
        Integer pneumoniaCasesCount,

        @Schema(description = "OO'RI — Jami.", example = "0")
        Integer sariTotalCount,

        @Schema(description = "OO'RI — Ulardan homiladorlar.", example = "0")
        Integer sariPregnantCount,

        @Schema(description = "O'lganlar — Jami.", example = "0")
        Integer deathTotalCount,

        @Schema(description = "O'lganlar — Ulardan homiladorlar.", example = "0")
        Integer deathPregnantCount,

        @Schema(description = "Vaktsinatsiya grippga qarshi — Haftalik kasallanish.", example = "0")
        Integer weeklyVaccinationCount,

        @Schema(description = "Vaktsinatsiya grippga qarshi — Mavsum boshidan.", example = "0")
        Integer seasonVaccinationCount,

        @Schema(description = "Дата создания записи.")
        Instant createdAt
) {
}
