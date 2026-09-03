package uz.uzinfocom.app.modules.reference.application.population.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Итог синхронизации справочника численности населения из stat.uz SDMX.")
public record PopulationSyncResult(

        @Schema(description = "Всего строк (территория × год) обработано из фида.", example = "442")
        int processed,

        @Schema(description = "Создано новых записей.", example = "221")
        int inserted,

        @Schema(description = "Обновлено существующих записей (источник SDMX).", example = "221")
        int updated,

        @Schema(description = "Пропущено записей с ручным вводом (source = MANUAL) — они не перезаписываются.", example = "0")
        int skippedManual,

        @Schema(description = "Территорий СОАТО из фида, не сопоставленных ни с одной активной областью/районом.", example = "1")
        int unmatched,

        @Schema(description = "Минимальный год, начиная с которого импортируются данные.", example = "2025")
        int minYear,

        @Schema(description = "Дата последнего изменения набора данных по метаданным stat.uz.", example = "2026-04-22")
        String sdmxLastModified
) {
}
