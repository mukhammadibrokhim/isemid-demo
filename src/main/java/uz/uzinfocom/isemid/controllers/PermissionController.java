package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Permission;
import uz.uzinfocom.isemid.models.payload.requests.ReqPermission;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.PermissionService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.PERMISSION;

@RestController
@RequestMapping(PERMISSION)
@Tag(name = "Permission", description = "This APIs for Permission services")
@RequiredArgsConstructor
public class PermissionController {

    final PermissionService permissionService;

    @PostMapping
    public ApiResponse<Permission> create(@Valid @RequestBody ReqPermission reqPermission) {
        return permissionService.create(reqPermission);
    }

    @GetMapping("/{id}")
    public ApiResponse<Permission> getById(@PathVariable long id) {
        return permissionService.getBydId(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<Permission> update(@PathVariable long id, @Valid @RequestBody ReqPermission reqPermission) {
        return permissionService.update(id, reqPermission);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable long id) {
        return permissionService.deleteById(id);
    }
}
