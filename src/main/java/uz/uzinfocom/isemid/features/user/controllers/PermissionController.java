package uz.uzinfocom.isemid.features.user.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.user.dtos.requests.PermissionRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.PermissionResponse;
import uz.uzinfocom.isemid.features.user.services.PermissionService;

@RestController
@RequestMapping(ApiPaths.Permission.ROOT)
@Tag(name = "Permission", description = "This APIs for Permission services")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionRequest permissionRequest) {
        return permissionService.create(permissionRequest);
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionResponse> getById(@PathVariable long id) {
        return permissionService.getBydId(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<PermissionResponse> update(@PathVariable long id, @Valid @RequestBody PermissionRequest permissionRequest) {
        return permissionService.update(id, permissionRequest);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable long id) {
        return permissionService.deleteById(id);
    }
}
