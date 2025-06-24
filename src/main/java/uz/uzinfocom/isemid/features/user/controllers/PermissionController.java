package uz.uzinfocom.isemid.features.user.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.features.user.dtos.requests.PermissionRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.PermissionResponse;
import uz.uzinfocom.isemid.features.user.services.PermissionService;

import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Permission.ROOT)
@Tag(name = "Permission", description = "This APIs for Permission services")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(
            summary = "Получить список разрешений (с пагинацией и фильтрацией)",
            description = "Возвращает постраничный список разрешений. Доступны фильтры по subject, description (на всех языках) и активности.")
    @GetMapping("/all")
    public PagedResponse<?> getPermissions(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String descriptionUz,
            @RequestParam(required = false) String descriptionRu,
            @RequestParam(required = false) String descriptionUzCyril,
            @RequestParam(required = false) String descriptionKaa,
            @RequestParam(required = false) Boolean active,
            HttpServletRequest request
    ) {
        return permissionService.
                getAllPermissionsPage(page, size, subject, descriptionUz, descriptionRu, descriptionUzCyril, descriptionKaa, active, getRequestUrl(request));
    }

    @PostMapping
    @Operation(summary = "Создать разрешение", description = "Создаёт новое разрешение с переданными параметрами")
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionRequest permissionRequest) {
        return permissionService.create(permissionRequest);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить разрешение по ID", description = "Возвращает разрешение по уникальному идентификатору")
    public ApiResponse<PermissionResponse> getById(@PathVariable long id) {
        return permissionService.getBydId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить разрешение", description = "Обновляет данные разрешения по ID")
    public ApiResponse<PermissionResponse> update(@PathVariable long id, @Valid @RequestBody PermissionRequest permissionRequest) {
        return permissionService.update(id, permissionRequest);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить разрешение", description = "Удаляет разрешение по ID")
    public ApiResponse<?> delete(@PathVariable long id) {
        return permissionService.deleteById(id);
    }
}
