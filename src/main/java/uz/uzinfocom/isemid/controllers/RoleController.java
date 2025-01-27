package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.RoleService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.ROLE;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;


@RestController
@RequestMapping(ROLE)
@RequiredArgsConstructor
@Tag(name = "Role", description = "API for Roles")
public class RoleController {

    final RoleService roleService;

    @GetMapping("/all")
    public ApiResponsePage getAllRoles(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String descriptionUz,
            @RequestParam(required = false) String descriptionUzCyril,
            @RequestParam(required = false) String descriptionRu,
            @RequestParam(required = false) String descriptionKaa,
            @CurrentUser User currentUser,
            HttpServletRequest request
    ) {
        return roleService.getAllRolesPage(page, size, name, descriptionUz, descriptionRu, descriptionUzCyril, descriptionKaa, getFullURL(request), currentUser);
    }

    @PutMapping("/{roleId}/permissions")
    public ApiResponse<Role> roleAddPermission(@PathVariable long roleId, @RequestParam String permissionName) {
        return roleService.roleAddPermission(roleId, permissionName);
    }

    @DeleteMapping("/{roleId}/permissions")
    public ApiResponse<Role> roleRemovePermission(@PathVariable long roleId, @RequestParam String permissionName) {
        return roleService.roleRemovePermission(roleId, permissionName);
    }
}
