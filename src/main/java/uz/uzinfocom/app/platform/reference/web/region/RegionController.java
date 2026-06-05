package uz.uzinfocom.app.platform.reference.web.region;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.reference.application.region.dto.RegionCreateRequest;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionFilterRequest;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionTableResponse;
import uz.uzinfocom.app.platform.reference.application.region.dto.RegionUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.region.command.RegionCommandService;
import uz.uzinfocom.app.platform.reference.application.region.query.RegionQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;

import java.util.List;

@Validated
@RestController
@RequestMapping(ApiPaths.Reference.REGIONS)
@RequiredArgsConstructor
public class RegionController {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final RegionQueryService regionQueryService;
    private final RegionCommandService regionCommandService;
    private final MessageResolver messageResolver;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<RegionTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute RegionFilterRequest request
    ) {
        Page<RegionTableResponse> page = regionQueryService.findTable(request);
        return PagedResponse.fromPage(page, messageResolver.resolve("common.success"));
    }

    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RegionResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.success(messageResolver.resolve("common.success"), regionQueryService.getById(id));
    }

    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RegionResponse> getByCode(
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), regionQueryService.getByCode(code));
    }

    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RegionResponse>> getByParentCode(
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                regionQueryService.getByParentCode(parentCode)
        );
    }

    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<RegionResponse> create(@Valid @RequestBody RegionCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), regionCommandService.create(request));
    }

    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<RegionResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody RegionUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), regionCommandService.update(id, request));
    }

    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        regionCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
