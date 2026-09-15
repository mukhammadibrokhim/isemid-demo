package uz.uzinfocom.app.modules.report.form32.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Строка ручной записи Shakl №3-2 для постраничного табличного ответа.")
public record Form32EntryTableResponse(
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

        @Schema(description = "Tekshirilgan obyektlar — Jami.", example = "0")
        Integer inspectedTotalCount,

        @Schema(description = "Tekshirilgan obyektlar — MTM.", example = "0")
        Integer inspectedMtmCount,

        @Schema(description = "Tekshirilgan obyektlar — Maktab.", example = "0")
        Integer inspectedSchoolCount,

        @Schema(description = "Tekshirilgan obyektlar — DPM.", example = "0")
        Integer inspectedDpmCount,

        @Schema(description = "Tekshirilgan obyektlar — Boshqa.", example = "0")
        Integer inspectedOtherCount,

        @Schema(description = "Aniqlangan kamchiliklar — Jami.", example = "0")
        Integer deficiencyTotalCount,

        @Schema(description = "Aniqlangan kamchiliklar — MTM.", example = "0")
        Integer deficiencyMtmCount,

        @Schema(description = "Aniqlangan kamchiliklar — Maktab.", example = "0")
        Integer deficiencySchoolCount,

        @Schema(description = "Aniqlangan kamchiliklar — DPM.", example = "0")
        Integer deficiencyDpmCount,

        @Schema(description = "Aniqlangan kamchiliklar — Boshqa.", example = "0")
        Integer deficiencyOtherCount,

        @Schema(description = "Mansabdor shaxslar — Jami.", example = "0")
        Integer officialTotalCount,

        @Schema(description = "Mansabdor shaxslar — MTM.", example = "0")
        Integer officialMtmCount,

        @Schema(description = "Mansabdor shaxslar — Maktab.", example = "0")
        Integer officialSchoolCount,

        @Schema(description = "Mansabdor shaxslar — DPM.", example = "0")
        Integer officialDpmCount,

        @Schema(description = "Mansabdor shaxslar — Boshqa.", example = "0")
        Integer officialOtherCount,

        @Schema(description = "To'xtatilgan obyektlar — Jami.", example = "0")
        Integer suspendedTotalCount,

        @Schema(description = "To'xtatilgan obyektlar — MTM.", example = "0")
        Integer suspendedMtmCount,

        @Schema(description = "To'xtatilgan obyektlar — Maktab.", example = "0")
        Integer suspendedSchoolCount,

        @Schema(description = "To'xtatilgan obyektlar — DPM.", example = "0")
        Integer suspendedDpmCount,

        @Schema(description = "To'xtatilgan obyektlar — Boshqa.", example = "0")
        Integer suspendedOtherCount,

        @Schema(description = "Дата создания записи.")
        Instant createdAt
) {
}
