package uz.uzinfocom.app.platform.reference.web.district;

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
import uz.uzinfocom.app.platform.reference.application.district.dto.DistrictCreateRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictResponse;
import uz.uzinfocom.app.platform.reference.application.district.dto.DistrictUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.district.command.DistrictCommandService;
import uz.uzinfocom.app.platform.reference.application.district.query.DistrictQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

import java.util.List;

@Validated
@RestController
@RequestMapping(ApiPaths.Reference.DISTRICTS)
@RequiredArgsConstructor
public class DistrictController {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final DistrictQueryService districtQueryService;
    private final DistrictCommandService districtCommandService;
    private final MessageResolver messageResolver;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<DistrictResponse>> getAll() {
        return ApiResponse.success(messageResolver.resolve("common.success"), districtQueryService.getAll());
    }

    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DistrictResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.success(messageResolver.resolve("common.success"), districtQueryService.getById(id));
    }

    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DistrictResponse> getByCode(
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), districtQueryService.getByCode(code));
    }

    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<DistrictResponse>> getByParentCode(
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                districtQueryService.getByParentCode(parentCode)
        );
    }

    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<DistrictResponse> create(@Valid @RequestBody DistrictCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), districtCommandService.create(request));
    }

    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<DistrictResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DistrictUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), districtCommandService.update(id, request));
    }

    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        districtCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
