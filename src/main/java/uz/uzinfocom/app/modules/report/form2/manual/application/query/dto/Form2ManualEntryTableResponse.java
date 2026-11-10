package uz.uzinfocom.app.modules.report.form2.manual.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Строка ручной записи Shakl №2 для постраничного табличного ответа.")
public record Form2ManualEntryTableResponse(
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

        @Schema(description = "O'tgan yilda qayd etilgan holatlar soni.", example = "12")
        Long registeredCasesLastYear,

        @Schema(description = "Joriy yilda qayd etilgan holatlar soni.", example = "15")
        Long registeredCasesCurrentYear,

        @Schema(description = "Hisobot davrida MTM va maktablarda aniqlangan holatlar soni.", example = "0")
        Integer mtmSchoolCount,

        @Schema(description = "Dezinfeksiya tadbirlari o'tkazilgan o'choqlar soni.", example = "0")
        Integer disinfectionFociCount,

        @Schema(description = "Tekshirilgan obyektlar soni.", example = "0")
        Integer inspectedObjectsCount,

        @Schema(description = "Yopilgan obyektlar soni.", example = "0")
        Integer closedObjectsCount,

        @Schema(description = "DDU soni.", example = "0")
        Integer dduCount,

        @Schema(description = "Maktablar soni.", example = "0")
        Integer schoolCount,

        @Schema(description = "Jarimalar soni.", example = "0")
        Integer finesCount,

        @Schema(description = "Prokuraturaga berilganlari soni.", example = "0")
        Integer prosecutorReferralCount,

        @Schema(description = "Дата создания записи.")
        Instant createdAt
) {
}
