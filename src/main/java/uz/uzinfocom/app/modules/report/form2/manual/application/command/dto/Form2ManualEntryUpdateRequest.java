package uz.uzinfocom.app.modules.report.form2.manual.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "Данные для обновления ручной записи Shakl №2. " +
        "registeredCasesLastYear/registeredCasesCurrentYear в запросе не передаются — " +
        "сервер пересчитывает их самостоятельно по from/to.")
public record Form2ManualEntryUpdateRequest(
        @Schema(description = "Начало периода отчёта.", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.form2_manual_entry.from.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @Schema(description = "Конец периода отчёта.", example = "2026-01-31", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{report.form2_manual_entry.to.required}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @Schema(description = "Hisobot davrida MTM va maktablarda aniqlangan holatlar soni.", example = "0")
        @PositiveOrZero(message = "{report.form2_manual_entry.count.positive_or_zero}")
        Integer mtmSchoolCount,

        @Schema(description = "Dezinfeksiya tadbirlari o'tkazilgan o'choqlar soni.", example = "0")
        @PositiveOrZero(message = "{report.form2_manual_entry.count.positive_or_zero}")
        Integer disinfectionFociCount,

        @Schema(description = "Tekshirilgan obyektlar soni.", example = "0")
        @PositiveOrZero(message = "{report.form2_manual_entry.count.positive_or_zero}")
        Integer inspectedObjectsCount,

        @Schema(description = "Yopilgan obyektlar soni.", example = "0")
        @PositiveOrZero(message = "{report.form2_manual_entry.count.positive_or_zero}")
        Integer closedObjectsCount,

        @Schema(description = "DDU soni.", example = "0")
        @PositiveOrZero(message = "{report.form2_manual_entry.count.positive_or_zero}")
        Integer dduCount,

        @Schema(description = "Maktablar soni.", example = "0")
        @PositiveOrZero(message = "{report.form2_manual_entry.count.positive_or_zero}")
        Integer schoolCount,

        @Schema(description = "Jarimalar soni.", example = "0")
        @PositiveOrZero(message = "{report.form2_manual_entry.count.positive_or_zero}")
        Integer finesCount,

        @Schema(description = "Prokuraturaga berilganlari soni.", example = "0")
        @PositiveOrZero(message = "{report.form2_manual_entry.count.positive_or_zero}")
        Integer prosecutorReferralCount
) {
}
