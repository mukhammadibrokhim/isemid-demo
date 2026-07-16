package uz.uzinfocom.app.platform.settings.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Tag(
        name = "Admin - Settings",
        description = "API для управления настройками системы, редактируемыми во время выполнения "
                + "(без переразвёртывания приложения)."
)
@Validated
@RestController
@RequestMapping(ApiPaths.SystemSetting.ROOT)
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingQueryService systemSettingQueryService;
    private final SystemSettingCommandService systemSettingCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить список настроек системы",
            description = "Возвращает постраничный список настроек с фильтрацией по ключу, типу значения "
                    + "и признаку активности."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<SystemSettingTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute SystemSettingFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<SystemSettingTableResponse> page = systemSettingQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить настройку по идентификатору",
            description = "Возвращает детальную информацию о настройке системы."
    )
    @GetMapping(ApiPaths.SystemSetting.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SystemSettingResponse> getById(
            @Parameter(description = "Внутренний идентификатор настройки.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), systemSettingQueryService.getById(id));
    }

    @Operation(
            summary = "Получить настройку по ключу",
            description = "Возвращает детальную информацию о настройке системы по её уникальному ключу."
    )
    @GetMapping(ApiPaths.SystemSetting.BY_KEY)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SystemSettingResponse> getByKey(
            @Parameter(description = "Уникальный ключ настройки.", required = true)
            @PathVariable("key") @NotBlank String key
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), systemSettingQueryService.getByKey(key));
    }

    @Operation(
            summary = "Создать настройку системы",
            description = "Создаёт новую настройку системы с уникальным ключом."
    )
    @PostMapping
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<SystemSettingResponse> create(
            @Valid @RequestBody SystemSettingCreateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                systemSettingCommandService.create(request)
        );
    }

    @Operation(
            summary = "Обновить настройку системы",
            description = "Обновляет значение, тип, описание и признак активности настройки. "
                    + "Ключ настройки после создания не изменяется."
    )
    @PutMapping(ApiPaths.SystemSetting.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<SystemSettingResponse> update(
            @Parameter(description = "Внутренний идентификатор настройки.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody SystemSettingUpdateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.updated"),
                systemSettingCommandService.update(id, request)
        );
    }

    @Operation(
            summary = "Удалить настройку системы",
            description = "Выполняет мягкое удаление настройки системы."
    )
    @DeleteMapping(ApiPaths.SystemSetting.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор настройки.", required = true)
            @PathVariable @Positive Long id
    ) {
        systemSettingCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }

    @Operation(
            summary = "Восстановить настройку системы",
            description = "Восстанавливает мягко удалённую настройку системы."
    )
    @PatchMapping(ApiPaths.SystemSetting.RESTORE)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> restore(
            @Parameter(description = "Внутренний идентификатор настройки.", required = true)
            @PathVariable @Positive Long id
    ) {
        systemSettingCommandService.restore(id);
        return ApiResponse.success(messageResolver.resolve("common.restored"), null);
    }
}
