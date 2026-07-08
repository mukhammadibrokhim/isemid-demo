package uz.uzinfocom.app.platform.reference.web.mkb10;

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
import uz.uzinfocom.app.platform.reference.application.mkb10.command.Mkb10CommandService;
import uz.uzinfocom.app.platform.reference.application.mkb10.dto.Mkb10CreateRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.dto.Mkb10UpdateRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.Mkb10QueryService;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10FilterRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10Response;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10TableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

import java.util.List;

@Tag(
        name = "Reference - MKB-10",
        description = "ICD-10 (MKB-10) classifier reference dictionary management APIs. Nodes are bulk-imported " +
                "from the external WHO ICD-10 hierarchy and keep the source's ids to preserve parent/child links."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.MKB10)
@RequiredArgsConstructor
public class Mkb10Controller {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final Mkb10QueryService mkb10QueryService;
    private final Mkb10CommandService mkb10CommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Get paginated MKB-10 classifier data",
            description = """
                    Returns active MKB-10 nodes as a paginated table.

                    Supported filters: code, name, parentId, level.
                    Pagination is 1-based.
                    Supported sort fields: id, code, level, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "MKB-10 nodes successfully retrieved."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Mkb10TableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute Mkb10FilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<Mkb10TableResponse> page = mkb10QueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Get top-level MKB-10 chapters",
            description = "Returns every active MKB-10 node with no parent (the top level of the classifier tree)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "MKB-10 root nodes successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.ROOTS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Mkb10Response>> getRoots() {
        return ApiResponse.success(messageResolver.resolve("common.success"), mkb10QueryService.getRoots());
    }

    @Operation(
            summary = "Get direct children of an MKB-10 node",
            description = "Returns every active MKB-10 node whose parent is the given node id, for tree navigation."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "MKB-10 child nodes successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.CHILDREN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Mkb10Response>> getChildren(
            @Parameter(description = "Parent MKB-10 node external id.", required = true, example = "12")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), mkb10QueryService.getChildren(id));
    }

    @Operation(
            summary = "Get MKB-10 node by id",
            description = "Returns a single active MKB-10 node by its external identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "MKB-10 node successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Mkb10Response> getById(
            @Parameter(description = "MKB-10 node external id.", required = true, example = "1500")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), mkb10QueryService.getById(id));
    }

    @Operation(
            summary = "Get MKB-10 node by code",
            description = "Returns a single active MKB-10 node by its normalized ICD-10 code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "MKB-10 node successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Mkb10Response> getByCode(
            @Parameter(description = "ICD-10 code.", required = true, example = "A15")
            @PathVariable @NotBlank @Size(max = 20) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), mkb10QueryService.getByCode(code));
    }

    @Operation(
            summary = "Create MKB-10 node",
            description = "Creates a new MKB-10 classifier node. The id must match the external WHO ICD-10 " +
                    "source's numbering so parent/child references stay valid across re-imports."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "MKB-10 node successfully created."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Mkb10Response> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "MKB-10 node data to create.",
                    required = true
            )
            @Valid @RequestBody Mkb10CreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), mkb10CommandService.create(request));
    }

    @Operation(
            summary = "Update MKB-10 node",
            description = "Updates an existing active MKB-10 node by its external identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "MKB-10 node successfully updated."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Mkb10Response> update(
            @Parameter(description = "MKB-10 node external id.", required = true, example = "1500")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New MKB-10 node data.",
                    required = true
            )
            @Valid @RequestBody Mkb10UpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), mkb10CommandService.update(id, request));
    }

    @Operation(
            summary = "Delete MKB-10 node",
            description = "Soft-deletes an MKB-10 node. Nodes with active children cannot be deleted."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "MKB-10 node successfully deleted."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "MKB-10 node external id.", required = true, example = "1500")
            @PathVariable @Positive Long id
    ) {
        mkb10CommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
