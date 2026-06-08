package uz.uzinfocom.app.platform.reference.web.neighborhood;

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
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

import java.util.List;

@Tag(
        name = "Reference - Neighborhoods",
        description = "Neighborhood reference dictionary management APIs."
)
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
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Get paginated neighborhood reference data",
            description = """
                    Returns active Neighborhood reference records as a paginated table.

                    Supported filters: code, name, soatoId.
                    Neighborhood.parentCode identifies the parent District code.
                    Pagination is 1-based.
                    Supported sort fields: id, code, parentCode, soatoId, parentSoatoId, nameUz, nameUzCyril, nameRu, nameKaa, sortOrder, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Neighborhoods successfully retrieved."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<NeighborhoodTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute NeighborhoodFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<NeighborhoodTableResponse> page = neighborhoodQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Get neighborhood by id",
            description = "Returns a single active Neighborhood reference record by its internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Neighborhood successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NeighborhoodResponse> getById(
            @Parameter(description = "Neighborhood internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), neighborhoodQueryService.getById(id));
    }

    @Operation(
            summary = "Get neighborhood by code",
            description = "Returns a single active Neighborhood reference record by the normalized neighborhood code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Neighborhood successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NeighborhoodResponse> getByCode(
            @Parameter(description = "Neighborhood code.", required = true, example = "AN-202001")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), neighborhoodQueryService.getByCode(code));
    }

    @Operation(
            summary = "Get neighborhoods by district code",
            description = "Returns active Neighborhood reference records whose parentCode matches the supplied District code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Neighborhoods successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NeighborhoodResponse>> getByParentCode(
            @Parameter(description = "District code stored in Neighborhood.parentCode.", required = true, example = "AN-202")
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                neighborhoodQueryService.getByParentCode(parentCode)
        );
    }

    @Operation(
            summary = "Create neighborhood",
            description = "Creates a new Neighborhood reference record under an existing District parent."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Neighborhood successfully created."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<NeighborhoodResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Neighborhood reference data to create.",
                    required = true
            )
            @Valid @RequestBody NeighborhoodCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), neighborhoodCommandService.create(request));
    }

    @Operation(
            summary = "Update neighborhood",
            description = "Updates an existing active Neighborhood reference record by internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Neighborhood successfully updated."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<NeighborhoodResponse> update(
            @Parameter(description = "Neighborhood internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New Neighborhood reference data.",
                    required = true
            )
            @Valid @RequestBody NeighborhoodUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), neighborhoodCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete neighborhood",
            description = "Soft-deletes a Neighborhood reference record. Deleted records are excluded from read endpoints."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Neighborhood successfully deleted."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "Neighborhood internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        neighborhoodCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
