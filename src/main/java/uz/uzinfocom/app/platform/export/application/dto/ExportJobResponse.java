package uz.uzinfocom.app.platform.export.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.export.domain.ExportStatus;

import java.time.Instant;

@Schema(description = "Фоновая задача Excel-экспорта.")
public record ExportJobResponse(
        @Schema(description = "Идентификатор задачи.")
        Long id,

        @Schema(description = "Тип экспорта, например FORM058.")
        String exportType,

        @Schema(description = "Статус задачи.")
        ExportStatus status,

        @Schema(description = "Процент выполнения (0-100).")
        int progressPercent,

        @Schema(description = "Число обработанных строк.")
        long processedRows,

        @Schema(description = "Общее число строк, подходящих под фильтр.")
        Long totalRows,

        @Schema(description = "Имя файла (доступно после завершения).")
        String fileName,

        @Schema(description = "Размер файла в байтах (доступен после завершения).")
        Long fileSizeBytes,

        @Schema(description = "Сообщение об ошибке, если задача завершилась неудачно.")
        String errorMessage,

        @Schema(description = "Дата и время постановки задачи.")
        Instant createdAt,

        @Schema(description = "Дата и время завершения задачи.")
        Instant completedAt
) {
}
