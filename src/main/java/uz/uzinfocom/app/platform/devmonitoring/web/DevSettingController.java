package uz.uzinfocom.app.platform.devmonitoring.web;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.settings.application.command.SystemSettingCommandService;
import uz.uzinfocom.app.platform.settings.application.dto.SystemSettingCreateRequest;
import uz.uzinfocom.app.platform.settings.application.dto.SystemSettingUpdateRequest;
import uz.uzinfocom.app.platform.settings.application.query.SystemSettingQueryService;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingFilterRequest;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingResponse;
import uz.uzinfocom.app.platform.settings.application.query.dto.SystemSettingTableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Dev-panel mirror of {@code SystemSettingController} - same command/query
 * services and the same {@code system_settings} table, reached via the
 * {@code DevUser} Basic-Auth chain instead of an SSO admin token. No
 * {@code @PreAuthorize} needed: the whole {@code /v1/dev/**} chain already
 * requires authentication (see {@code DevPanelSecurityConfig}). A value
 * written from either surface is visible on the other immediately.
 */
@Tag(
        name = "Dev Monitoring - Settings",
        description = "Runtime system-settings configuration (key/value store) reachable from the developer "
                + "monitoring panel - same table as /v1/admin/settings."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevSettingController {

    private final SystemSettingQueryService systemSettingQueryService;
    private final SystemSettingCommandService systemSettingCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @GetMapping(ApiPaths.Dev.SETTINGS)
    public PagedResponse<SystemSettingTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute SystemSettingFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<SystemSettingTableResponse> page = systemSettingQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @PostMapping(ApiPaths.Dev.SETTINGS)
    public ApiResponse<SystemSettingResponse> create(@Valid @RequestBody SystemSettingCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), systemSettingCommandService.create(request));
    }

    @GetMapping(ApiPaths.Dev.SETTINGS_BY_ID)
    public ApiResponse<SystemSettingResponse> getById(
            @Parameter(description = "Internal id of the setting.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), systemSettingQueryService.getById(id));
    }

    @PutMapping(ApiPaths.Dev.SETTINGS_BY_ID)
    public ApiResponse<SystemSettingResponse> update(
            @Parameter(description = "Internal id of the setting.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody SystemSettingUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), systemSettingCommandService.update(id, request));
    }

    @DeleteMapping(ApiPaths.Dev.SETTINGS_BY_ID)
    public ApiResponse<Void> delete(
            @Parameter(description = "Internal id of the setting.", required = true)
            @PathVariable @Positive Long id
    ) {
        systemSettingCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"));
    }

    @PatchMapping(ApiPaths.Dev.SETTINGS_RESTORE)
    public ApiResponse<Void> restore(
            @Parameter(description = "Internal id of the setting.", required = true)
            @PathVariable @Positive Long id
    ) {
        systemSettingCommandService.restore(id);
        return ApiResponse.success(messageResolver.resolve("common.restored"));
    }

    @GetMapping(ApiPaths.Dev.SETTINGS_BY_KEY)
    public ApiResponse<SystemSettingResponse> getByKey(
            @Parameter(description = "Unique key of the setting.", required = true)
            @PathVariable("key") @NotBlank String key
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), systemSettingQueryService.getByKey(key));
    }
}
