package uz.uzinfocom.isemid.features.user.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.user.dtos.requests.RoleRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.RoleResponse;
import uz.uzinfocom.isemid.features.user.mappers.RoleMapper;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.services.RoleService;

import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Role.ROOT)
@Tag(name = "Role", description = "API для управления ролями пользователей")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final MessageUtil messageUtil;
    private final RoleMapper roleMapper;

    @Operation(summary = "Получить постраничный список ролей с фильтрами", description = "Возвращает постраничный список ролей, отфильтрованных по необязательным параметрам, таким как имя и описания на разных языках.")
    @GetMapping("/all")
    public PagedResponse<?> getAllRolesByFilter(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String descriptionUz,
            @RequestParam(required = false) String descriptionUzCyril,
            @RequestParam(required = false) String descriptionRu,
            @RequestParam(required = false) String descriptionKaa,
            HttpServletRequest request
    ) {
        Page<Role> paged = roleService.filter(page, size, name, descriptionUz, descriptionRu, descriptionUzCyril, descriptionKaa);
        return PagedResponse.build(page, paged.map(roleMapper::toResponse), getRequestUrl(request), messageUtil);
    }

    @Operation(
            summary = "Поиск ролей по тексту",
            description = "Ищет до 10 ролей по имени или описаниям на всех языках. Поиск нечувствителен к регистру.")
    @GetMapping
    public ApiResponse<?> getAllRolesBySearch(@RequestParam(required = false) String search) {
        return ApiResponse.success(messageUtil, roleService.roleSearch(search).stream().map(roleMapper::toResponse));
    }

    @Operation(
            summary = "Создать новую роль",
            description = "Создает новую роль с указанными данными.")
    @PostMapping
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        return ApiResponse.success(messageUtil, roleMapper.toResponse(roleService.create(roleRequest)));
    }

    @Operation(
            summary = "Обновить существующую роль",
            description = "Обновляет роль по идентификатору ID новыми данными.")
    @PreAuthorize("hasRole('isemid_super_admin')")
    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> updateRole(@PathVariable long id, @RequestBody RoleRequest roleRequest) {
        return ApiResponse.success(messageUtil, roleMapper.toResponse(roleService.update(id, roleRequest)));
    }

    @Operation(
            summary = "Удалить роль",
            description = "Удаляет роль по указанному ID.")
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRole(@PathVariable long id) {
        roleService.deleteById(id);
        return ApiResponse.deleteSuccess(messageUtil);
    }

    @Operation(
            summary = "Добавить разрешение к роли",
            description = "Прикрепляет разрешение с ID к роли с указанным ID.")
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ApiResponse<?> addPermission(@PathVariable long roleId, @PathVariable long permissionId) {
        return ApiResponse.success(messageUtil, roleMapper.toResponse(roleService.addPermission(roleId, permissionId)));
    }

    @Operation(
            summary = "Удалить разрешение у роли",
            description = "Удаляет разрешение с ID у роли с указанным ID.")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ApiResponse<?> removePermission(@PathVariable long roleId, @PathVariable long permissionId) {
        return ApiResponse.success(messageUtil, roleMapper.toResponse(roleService.removePermission(roleId, permissionId)));
    }
}