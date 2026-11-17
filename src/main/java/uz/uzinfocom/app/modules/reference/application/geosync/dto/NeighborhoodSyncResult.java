package uz.uzinfocom.app.modules.reference.application.geosync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Итог синхронизации махаллей (ref_neighborhood) с реестром классификаторов cs.egov.uz. "
        + "Ключ сопоставления — TIN, запасной — soato_id.")
public record NeighborhoodSyncResult(

        @Schema(description = "true — пробный запуск: всё посчитано, но ничего не сохранено.", example = "false")
        boolean dryRun,

        @Schema(description = "Махаллей в классификаторе egov.", example = "8993")
        int received,

        @Schema(description = "Создано новых махаллей.", example = "0")
        int created,

        @Schema(description = "Мягко удалённых махаллей, возвращённых в работу.", example = "0")
        int reactivated,

        @Schema(description = "Махаллей, у которых изменился район (parent_code / parent_soato_id).", example = "0")
        int moved,

        @Schema(description = "Махаллей с изменёнными прочими полями (названия, TIN, soato_id, код 1C). "
                + "Строка может учитываться и в moved.", example = "0")
        int fieldsUpdated,

        @Schema(description = "Махаллей без каких-либо изменений.", example = "8900")
        int unchanged,

        @Schema(description = "Пропущено: некорректная строка (пустой TIN, нулевой код uzcad/района).", example = "2")
        int skippedInvalid,

        @Schema(description = "Пропущено: активный район махалли не найден у нас (сначала синхронизируйте районы).",
                example = "53")
        int skippedUnknownDistrict,

        @Schema(description = "Пропущено: строка egov указывает на уже обработанную в этом запуске махаллю.", example = "0")
        int skippedDuplicate,

        @Schema(description = "Районы egov, не найденные среди наших активных районов, и число их махаллей.")
        List<UnresolvedDistrict> unresolvedDistricts,

        @Schema(description = "Районы, найденные по сокращённому коду egov: egov отдаёт 7-значный код "
                + "(например 1714365), а у нас район хранится с 10-значным (1714401365). Информационно.")
        List<AliasedDistrict> aliasedDistricts,

        @Schema(description = "TIN некорректных строк (не более 100).")
        List<String> invalidTins
) {

    public record UnresolvedDistrict(Integer soatoId, int neighborhoods) {
    }

    public record AliasedDistrict(Integer egovSoatoId, String districtCode, Integer districtSoatoId, int neighborhoods) {
    }
}
