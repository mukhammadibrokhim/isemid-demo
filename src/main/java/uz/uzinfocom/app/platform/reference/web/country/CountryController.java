package uz.uzinfocom.app.platform.reference.web.country;

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
import uz.uzinfocom.app.platform.reference.application.country.dto.CountryCreateRequest;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryFilterRequest;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryDetailedResponse;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryTableResponse;
import uz.uzinfocom.app.platform.reference.application.country.dto.CountryUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.country.command.CountryCommandService;
import uz.uzinfocom.app.platform.reference.application.country.query.CountryQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

@Tag(
        name = "Reference - Countries",
        description = "Country reference dictionary management APIs."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.COUNTRIES)
@RequiredArgsConstructor
public class CountryController {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final CountryQueryService countryQueryService;
    private final CountryCommandService countryCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Get paginated country reference data",
            description = """
                    Returns active Country reference records as a paginated table.

                    Supported filters: code, name.
                    Pagination is 1-based.
                    Supported sort fields: id, code, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Countries successfully retrieved."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<CountryTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute CountryFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<CountryTableResponse> page = countryQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Get country by id",
            description = "Returns a single active Country reference record by its internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Country successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CountryDetailedResponse> getById(
            @Parameter(description = "Country internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), countryQueryService.getById(id));
    }

    @Operation(
            summary = "Get country by code",
            description = "Returns a single active Country reference record by the normalized country code."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Country successfully retrieved."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CountryDetailedResponse> getByCode(
            @Parameter(description = "Country code.", required = true, example = "UZB")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), countryQueryService.getByCode(code));
    }

    @Operation(
            summary = "Create country",
            description = "Creates a new Country reference record. Codes are normalized before persistence."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Country successfully created."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CountryDetailedResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Country reference data to create.",
                    required = true
            )
            @Valid @RequestBody CountryCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), countryCommandService.create(request));
    }

    @Operation(
            summary = "Update country",
            description = "Updates an existing active Country reference record by internal identifier."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Country successfully updated."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CountryDetailedResponse> update(
            @Parameter(description = "Country internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New Country reference data.",
                    required = true
            )
            @Valid @RequestBody CountryUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), countryCommandService.update(id, request));
    }

    @Operation(
            summary = "Delete country",
            description = "Soft-deletes a Country reference record. Deleted records are excluded from read endpoints."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Country successfully deleted."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "Country internal identifier.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        countryCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
