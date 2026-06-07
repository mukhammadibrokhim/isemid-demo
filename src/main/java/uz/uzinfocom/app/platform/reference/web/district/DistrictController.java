package uz.uzinfocom.app.platform.reference.web.district;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import uz.uzinfocom.app.platform.reference.application.district.dto.DistrictCreateRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictFilterRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictTableResponse;
import uz.uzinfocom.app.platform.reference.application.district.dto.DistrictUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.district.command.DistrictCommandService;
import uz.uzinfocom.app.platform.reference.application.district.query.DistrictQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;

import java.util.List;

@Tag(
        name = "Reference - Districts",
        description = "District reference dictionary management APIs."
)
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

    @Operation(
            summary = "Get paginated district reference data",
            description = """
                    Returns active District reference records as a paginated table.

                    Supported filters: code, name, soatoId.
                    District.parentCode identifies the parent Region code.
                    Pagination is 1-based.
                    Supported sort fields: id, code, parentCode, soatoId, parentSoatoId, nameUz, nameUzCyril, nameRu, nameKaa, sortOrder, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Districts successfully retrieved."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<DistrictTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute DistrictFilterRequest request
    ) {
        Page<DistrictTableResponse> page = districtQueryService.findTable(request);
        return PagedResponse.fromPage(page, messageResolver.resolve("common.success"));
    }

    @Operation(
            summary = "Get district by id",
            description = "Returns a single active District reference record by its internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "District successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DistrictResponse> getById(
            @Parameter(description = "District internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), districtQueryService.getById(id));
    }

    @Operation(
            summary = "Get district by code",
            description = "Returns a single active District reference record by the normalized district code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "District successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DistrictResponse> getByCode(
            @Parameter(description = "District code.", required = true, example = "AN-202")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), districtQueryService.getByCode(code));
    }

    @Operation(
            summary = "Get districts by region code",
            description = "Returns active District reference records whose parentCode matches the supplied Region code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Districts successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<DistrictResponse>> getByParentCode(
            @Parameter(description = "Region code stored in District.parentCode.", required = true, example = "UZ-AN")
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                districtQueryService.getByParentCode(parentCode)
        );
    }

    @Operation(
            summary = "Create district",
            description = "Creates a new District reference record under an existing Region parent."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "District successfully created."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<DistrictResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "District reference data to create.",
                    required = true
            )
            @Valid @RequestBody DistrictCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), districtCommandService.create(request));
    }

    @Operation(
            summary = "Update district",
            description = "Updates an existing active District reference record by internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "District successfully updated."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<DistrictResponse> update(
            @Parameter(description = "District internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New District reference data.",
                    required = true
            )
            @Valid @RequestBody DistrictUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), districtCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete district",
            description = "Soft-deletes a District reference record. Deleted records are excluded from read endpoints."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "District successfully deleted."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "District internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        districtCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
