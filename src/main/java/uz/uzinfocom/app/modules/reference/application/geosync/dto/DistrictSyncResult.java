package uz.uzinfocom.app.modules.reference.application.geosync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Итог синхронизации районов (ref_district) с реестром классификаторов cs.egov.uz. "
        + "Ключ сопоставления — soato_id.")
public record DistrictSyncResult(

        @Schema(description = "true — пробный запуск: всё посчитано, но ничего не сохранено.", example = "false")
        boolean dryRun,

        @Schema(description = "Районов в списке СОАТО egov.", example = "205")
        int received,

        @Schema(description = "Создано новых районов.", example = "0")
        int created,

        @Schema(description = "Мягко удалённых районов, возвращённых в работу (deleted = false).", example = "0")
        int reactivated,

        @Schema(description = "Активных районов без изменений.", example = "205")
        int unchanged,

        @Schema(description = "Созданные районы. Egov отдаёт только русское название: uz/uz-Cyrl/kaa временно "
                + "заполнены им же — названия нужно перевести вручную.")
        List<CreatedDistrict> createdDistricts,

        @Schema(description = "Коды возвращённых в работу районов.")
        List<String> reactivatedDistricts,

        @Schema(description = "Районы egov, которые не удалось создать (нет области / конфликт кода).")
        List<SkippedDistrict> skippedDistricts
) {

    public record CreatedDistrict(String code, Integer soatoId, String nameRu) {
    }

    public record SkippedDistrict(String soatoId, String reason) {
    }
}
