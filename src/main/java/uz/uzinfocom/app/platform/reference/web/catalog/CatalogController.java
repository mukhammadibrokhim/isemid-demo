package uz.uzinfocom.app.platform.reference.web.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import uz.uzinfocom.app.platform.reference.application.catalog.command.CatalogCommandService;
import uz.uzinfocom.app.platform.reference.application.catalog.dto.CatalogCreateRequest;
import uz.uzinfocom.app.platform.reference.application.catalog.dto.CatalogUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.catalog.query.CatalogQueryService;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogFilterRequest;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogTableResponse;
import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;

import java.util.List;

@Tag(
        name = "Reference - Catalogs",
        description = "General reference catalog management APIs."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.CATALOGS)
@RequiredArgsConstructor
public class CatalogController {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final CatalogQueryService catalogQueryService;
    private final CatalogCommandService catalogCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Get paginated catalog reference data",
            description = """
                    Returns active Catalog reference records as a paginated table.

                    Supported filters: type, code, parentCode, search.
                    Catalog parentCode points to another item in the same catalog type.
                    Pagination is 1-based.
                    Supported sort fields: id, type, code, parentCode, nameUz, nameRu, sortOrder.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Catalog items successfully retrieved."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<CatalogTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute CatalogFilterRequest request
    ) {
        Page<CatalogTableResponse> page = catalogQueryService.findTable(request);
        return PagedResponse.fromPage(page, messageResolver.resolve("common.success"));
    }

    @Operation(
            summary = "Get catalog item by id",
            description = "Returns a single active Catalog reference record by its internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Catalog item successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CatalogResponse> getById(
            @Parameter(description = "Catalog item internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), catalogQueryService.getById(id));
    }

    @Operation(
            summary = "Get catalog item by type and code",
            description = "Returns a single active Catalog reference record by catalog type and normalized item code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Catalog item successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_TYPE_AND_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CatalogResponse> getByTypeAndCode(
            @Parameter(description = "Catalog type.", required = true, example = "GENDER")
            @PathVariable @NotNull CatalogType type,
            @Parameter(description = "Catalog item code.", required = true)
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                catalogQueryService.getByTypeAndCode(type, code)
        );
    }

    @Operation(
            summary = "Get catalog items by type",
            description = "Returns active Catalog reference records for the supplied catalog type, ordered by sort order and Uzbek name."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Catalog items successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_TYPE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CatalogResponse>> getByType(
            @Parameter(description = "Catalog type.", required = true, example = "GENDER")
            @PathVariable @NotNull CatalogType type
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), catalogQueryService.getByType(type));
    }

    @Operation(
            summary = "Get catalog items by type and parent code",
            description = "Returns active Catalog reference records in the supplied catalog type whose parentCode matches another item in the same catalog type."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Catalog items successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_TYPE_AND_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CatalogResponse>> getByTypeAndParentCode(
            @Parameter(description = "Catalog type.", required = true, example = "GENDER")
            @PathVariable @NotNull CatalogType type,
            @Parameter(description = "Parent catalog item code in the same catalog type.", required = true)
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                catalogQueryService.getByTypeAndParentCode(type, parentCode)
        );
    }

    @Operation(
            summary = "Create catalog item",
            description = "Creates a new Catalog reference record. Codes and optional parent codes are normalized before persistence."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Catalog item successfully created."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CatalogResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Catalog reference data to create.",
                    required = true
            )
            @Valid @RequestBody CatalogCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), catalogCommandService.create(request));
    }

    @Operation(
            summary = "Update catalog item",
            description = "Updates an existing active Catalog reference record by internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Catalog item successfully updated."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CatalogResponse> update(
            @Parameter(description = "Catalog item internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New Catalog reference data.",
                    required = true
            )
            @Valid @RequestBody CatalogUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), catalogCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete catalog item",
            description = "Soft-deletes a Catalog reference record. Deleted records are excluded from read endpoints."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Catalog item successfully deleted."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "Catalog item internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        catalogCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
