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
import uz.uzinfocom.app.modules.reference.application.geosync.dto.DistrictSyncResult;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

/** Super-admin trigger for the district sync from the e-government classifier registry. */
@Tag(
        name = "Reference - District sync",
        description = "Синхронизация справочника районов с реестром классификаторов электронного правительства "
                + "(cs.egov.uz), только для супер-администратора."
)
@RestController
@RequestMapping(ApiPaths.Reference.DISTRICTS)
@RequiredArgsConstructor
public class DistrictSyncController {

    private final GeoReferenceSyncService geoReferenceSyncService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Синхронизировать районы из cs.egov.uz",
            description = "Ключ — soato_id. Район есть и активен — не трогается; мягко удалён — возвращается в "
                    + "работу; нет — создаётся (egov отдаёт только русское название, остальные языки временно "
                    + "заполняются им же). Районы, которых нет в egov, не трогаются. dryRun=true — только расчёт "
                    + "без сохранения."
    )
    @PostMapping(ApiPaths.Reference.SYNC)
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<DistrictSyncResult> sync(
            @Parameter(description = "true — пробный запуск без сохранения изменений.")
            @RequestParam(defaultValue = "false") boolean dryRun
    ) {
        DistrictSyncResult result = geoReferenceSyncService.syncDistricts(dryRun);
        String messageCode = dryRun ? "reference.geo_sync.dry_run_success" : "reference.district.sync.success";
        return ApiResponse.success(messageResolver.resolve(messageCode), result);
    }
}
