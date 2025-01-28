package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        return permissionService.createPermission(reqPermission);
    }
}
