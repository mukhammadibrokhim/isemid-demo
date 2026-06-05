package uz.uzinfocom.app.platform.reference.web.neighborhood;

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
import uz.uzinfocom.app.platform.reference.application.neighborhood.dto.NeighborhoodCreateRequest;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodFilterRequest;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodTableResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.dto.NeighborhoodUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.neighborhood.command.NeighborhoodCommandService;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.NeighborhoodQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;

import java.util.List;

@Validated
@RestController
@RequestMapping(ApiPaths.Reference.NEIGHBORHOODS)
@RequiredArgsConstructor
public class NeighborhoodController {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final NeighborhoodQueryService neighborhoodQueryService;
    private final NeighborhoodCommandService neighborhoodCommandService;
    private final MessageResolver messageResolver;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<NeighborhoodTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute NeighborhoodFilterRequest request
    ) {
        Page<NeighborhoodTableResponse> page = neighborhoodQueryService.findTable(request);
        return PagedResponse.fromPage(page, messageResolver.resolve("common.success"));
    }

    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NeighborhoodResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.success(messageResolver.resolve("common.success"), neighborhoodQueryService.getById(id));
    }

    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NeighborhoodResponse> getByCode(
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), neighborhoodQueryService.getByCode(code));
    }

    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NeighborhoodResponse>> getByParentCode(
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                neighborhoodQueryService.getByParentCode(parentCode)
        );
    }

    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<NeighborhoodResponse> create(@Valid @RequestBody NeighborhoodCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), neighborhoodCommandService.create(request));
    }

    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<NeighborhoodResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody NeighborhoodUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), neighborhoodCommandService.update(id, request));
    }

    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        neighborhoodCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
