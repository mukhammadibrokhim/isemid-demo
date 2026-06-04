package uz.uzinfocom.app.platform.reference.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.reference.dto.MahallaCreateRequest;
import uz.uzinfocom.app.platform.reference.dto.MahallaResponse;
import uz.uzinfocom.app.platform.reference.dto.MahallaUpdateRequest;
import uz.uzinfocom.app.platform.reference.service.MahallaCommandService;
import uz.uzinfocom.app.platform.reference.service.MahallaQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

import java.util.List;

@Validated
@RestController
@RequestMapping(ApiPaths.Reference.MAHALLAS)
@RequiredArgsConstructor
public class MahallaController {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final MahallaQueryService mahallaQueryService;
    private final MahallaCommandService mahallaCommandService;
    private final MessageResolver messageResolver;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MahallaResponse>> getAll() {
        return ApiResponse.success(messageResolver.resolve("common.success"), mahallaQueryService.getAll());
    }

    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MahallaResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.success(messageResolver.resolve("common.success"), mahallaQueryService.getById(id));
    }

    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MahallaResponse> getByCode(
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), mahallaQueryService.getByCode(code));
    }

    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MahallaResponse>> getByParentCode(
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                mahallaQueryService.getByParentCode(parentCode)
        );
    }

    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<MahallaResponse> create(@Valid @RequestBody MahallaCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), mahallaCommandService.create(request));
    }

    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<MahallaResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody MahallaUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), mahallaCommandService.update(id, request));
    }

    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        mahallaCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
