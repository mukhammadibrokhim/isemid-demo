package uz.uzinfocom.app.platform.reference.web.country;

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
import uz.uzinfocom.app.platform.reference.application.country.dto.CountryCreateRequest;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryResponse;
import uz.uzinfocom.app.platform.reference.application.country.dto.CountryUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.country.command.CountryCommandService;
import uz.uzinfocom.app.platform.reference.application.country.query.CountryQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

import java.util.List;

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

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CountryResponse>> getAll() {
        return ApiResponse.success(messageResolver.resolve("common.success"), countryQueryService.getAll());
    }

    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CountryResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.success(messageResolver.resolve("common.success"), countryQueryService.getById(id));
    }

    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CountryResponse> getByCode(
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), countryQueryService.getByCode(code));
    }

    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CountryResponse> create(@Valid @RequestBody CountryCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), countryCommandService.create(request));
    }

    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CountryResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CountryUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), countryCommandService.update(id, request));
    }

    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        countryCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
