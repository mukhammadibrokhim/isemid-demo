package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devpanel.application.command.DevLegacyMigrationService;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLegacyMigrationResponse;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevLegacyMigrationStartRequest;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

@Tag(
        name = "Dev Panel - Legacy migration",
        description = "One-off legacy (public) -> public2 data migration. ROLE_DEV_SUPER_ADMIN only, and only "
                + "while app.legacy-migration.enabled=true. A real run TRUNCATEs the public2 business tables - "
                + "run with dryRun first."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevLegacyMigrationController {

    private final DevLegacyMigrationService service;
    private final MessageResolver messageResolver;

    @Operation(summary = "Start a migration run (dryRun defaults to true; a real run needs confirm=yes)")
    @PostMapping(ApiPaths.Dev.LEGACY_MIGRATION)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<DevLegacyMigrationResponse> start(
            @RequestBody(required = false) DevLegacyMigrationStartRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), service.start(request));
    }

    @Operation(summary = "Progress and report of the latest run (null data if none ran since startup)")
    @GetMapping(ApiPaths.Dev.LEGACY_MIGRATION_STATUS)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<DevLegacyMigrationResponse> status() {
        return ApiResponse.success(messageResolver.resolve("common.success"), service.status());
    }
}
