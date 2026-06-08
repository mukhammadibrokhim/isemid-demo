package uz.uzinfocom.app.platform.reference.web.region;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

import java.util.List;

@Tag(
        name = "Reference - Regions",
        description = "Region reference dictionary management APIs."
)
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
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Get paginated region reference data",
            description = """
                    Returns active Region reference records as a paginated table.

                    Supported filters: code, name, soatoId.
                    Region.parentCode identifies the parent Country code.
                    Pagination is 1-based.
                    Supported sort fields: id, code, parentCode, soatoId, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Regions successfully retrieved."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<RegionTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute RegionFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<RegionTableResponse> page = regionQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Get region by id",
            description = "Returns a single active Region reference record by its internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Region successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RegionResponse> getById(
            @Parameter(description = "Region internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), regionQueryService.getById(id));
    }

    @Operation(
            summary = "Get region by code",
            description = "Returns a single active Region reference record by the normalized region code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Region successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RegionResponse> getByCode(
            @Parameter(description = "Region code.", required = true, example = "UZ-AN")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), regionQueryService.getByCode(code));
    }

    @Operation(
            summary = "Get regions by country code",
            description = "Returns active Region reference records whose parentCode matches the supplied Country code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Regions successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RegionResponse>> getByParentCode(
            @Parameter(description = "Country code stored in Region.parentCode.", required = true, example = "UZ")
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                regionQueryService.getByParentCode(parentCode)
        );
    }

    @Operation(
            summary = "Create region",
            description = "Creates a new Region reference record under an existing Country parent."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Region successfully created."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<RegionResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Region reference data to create.",
                    required = true
            )
            @Valid @RequestBody RegionCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), regionCommandService.create(request));
    }

    @Operation(
            summary = "Update region",
            description = "Updates an existing active Region reference record by internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Region successfully updated."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<RegionResponse> update(
            @Parameter(description = "Region internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New Region reference data.",
                    required = true
            )
            @Valid @RequestBody RegionUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), regionCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete region",
            description = "Soft-deletes a Region reference record. Deleted records are excluded from read endpoints."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Region successfully deleted."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "Region internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        regionCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
