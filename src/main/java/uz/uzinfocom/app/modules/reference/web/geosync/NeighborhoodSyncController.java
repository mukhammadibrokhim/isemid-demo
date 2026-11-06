package uz.uzinfocom.app.modules.reference.web.geosync;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.reference.application.geosync.GeoReferenceSyncService;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.NeighborhoodSyncResult;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

/** Super-admin trigger for the neighborhood (mahalla) sync from the e-government classifier registry. */
@Tag(
        name = "Reference - Neighborhood sync",
        description = "Синхронизация справочника махаллей с реестром классификаторов электронного правительства "
                + "(cs.egov.uz), только для супер-администратора."
)
@RestController
@RequestMapping(ApiPaths.Reference.NEIGHBORHOODS)
@RequiredArgsConstructor
public class NeighborhoodSyncController {

    private final GeoReferenceSyncService geoReferenceSyncService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Синхронизировать махалли из cs.egov.uz",
            description = "Ключ — TIN, запасной — soato_id. Найденная махалля обновляется по изменённым полям "
                    + "(названия, TIN, soato_id, код 1C, район) и, если была мягко удалена, возвращается в работу; "
                    + "отсутствующая — создаётся с кодом «код района + 3-значный номер». Записи, которых нет в egov, "
                    + "не трогаются. Махалли районов, которых нет среди активных у нас, пропускаются — сначала "
                    + "синхронизируйте районы. dryRun=true — только расчёт без сохранения."
    )
    @PostMapping(ApiPaths.Reference.SYNC)
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<NeighborhoodSyncResult> sync(
            @Parameter(description = "true — пробный запуск без сохранения изменений.")
            @RequestParam(defaultValue = "false") boolean dryRun
    ) {
        NeighborhoodSyncResult result = geoReferenceSyncService.syncNeighborhoods(dryRun);
        String messageCode = dryRun ? "reference.geo_sync.dry_run_success" : "reference.neighborhood.sync.success";
        return ApiResponse.success(messageResolver.resolve(messageCode), result);
    }
}
