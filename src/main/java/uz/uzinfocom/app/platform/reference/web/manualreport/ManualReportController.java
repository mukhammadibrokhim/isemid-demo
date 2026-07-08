package uz.uzinfocom.app.platform.reference.web.manualreport;

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
import uz.uzinfocom.app.platform.reference.application.manualreport.command.ManualReportCommandService;
import uz.uzinfocom.app.platform.reference.application.manualreport.dto.ManualReportCreateRequest;
import uz.uzinfocom.app.platform.reference.application.manualreport.dto.ManualReportUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.ManualReportQueryService;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportFilterRequest;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportTableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

import java.util.List;

@Tag(
        name = "Reference - Manual Reports",
        description = "Manual report reference dictionary management APIs. Manual reports group ICD-10 (MKB-10) " +
                "diagnosis codes into named, admin-configured report buckets used for epidemiological reporting."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.MANUAL_REPORTS)
@RequiredArgsConstructor
public class ManualReportController {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final ManualReportQueryService manualReportQueryService;
    private final ManualReportCommandService manualReportCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Get paginated manual report reference data",
            description = """
                    Returns active Manual Report reference records as a paginated table.

                    Supported filters: code, name.
                    Pagination is 1-based.
                    Supported sort fields: id, code, shortName, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Manual reports successfully retrieved."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<ManualReportTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute ManualReportFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<ManualReportTableResponse> page = manualReportQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Get manual report by id",
            description = "Returns a single active Manual Report reference record by its internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Manual report successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ManualReportResponse> getById(
            @Parameter(description = "Manual Report internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), manualReportQueryService.getById(id));
    }

    @Operation(
            summary = "Get manual report by code",
            description = "Returns a single active Manual Report reference record by its normalized code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Manual report successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ManualReportResponse> getByCode(
            @Parameter(description = "Manual Report code.", required = true, example = "TUBERCULOSIS")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), manualReportQueryService.getByCode(code));
    }

    @Operation(
            summary = "Get manual reports by ICD-10 (MKB-10) code",
            description = "Returns every active Manual Report whose assigned MKB-10 code set contains the given " +
                    "diagnosis code. Used to resolve which report bucket(s) a Form058 diagnosis counts toward."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Manual reports successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_MKB10_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ManualReportResponse>> getByMkb10Code(
            @Parameter(description = "ICD-10 (MKB-10) diagnosis code.", required = true, example = "A15")
            @PathVariable @NotBlank @Size(max = 20) String code
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                manualReportQueryService.getByMkb10Code(code)
        );
    }

    @Operation(
            summary = "Create manual report",
            description = "Creates a new Manual Report reference record. The code is normalized and MKB-10 codes " +
                    "are trimmed and upper-cased before persistence."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Manual report successfully created."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<ManualReportResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Manual Report reference data to create.",
                    required = true
            )
            @Valid @RequestBody ManualReportCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), manualReportCommandService.create(request));
    }

    @Operation(
            summary = "Update manual report",
            description = "Updates an existing active Manual Report reference record by internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Manual report successfully updated."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<ManualReportResponse> update(
            @Parameter(description = "Manual Report internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New Manual Report reference data.",
                    required = true
            )
            @Valid @RequestBody ManualReportUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), manualReportCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete manual report",
            description = "Soft-deletes a Manual Report reference record. Deleted records are excluded from read endpoints."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Manual report successfully deleted."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "Manual Report internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        manualReportCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
