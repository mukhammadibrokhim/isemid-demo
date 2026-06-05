package uz.uzinfocom.app.platform.reference.web.catalog;

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

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<CatalogTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute CatalogFilterRequest request
    ) {
        Page<CatalogTableResponse> page = catalogQueryService.findTable(request);
        return PagedResponse.fromPage(page, messageResolver.resolve("common.success"));
    }

    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CatalogResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.success(messageResolver.resolve("common.success"), catalogQueryService.getById(id));
    }

    @GetMapping(ApiPaths.Reference.BY_TYPE_AND_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CatalogResponse> getByTypeAndCode(
            @PathVariable @NotNull CatalogType type,
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                catalogQueryService.getByTypeAndCode(type, code)
        );
    }

    @GetMapping(ApiPaths.Reference.BY_TYPE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CatalogResponse>> getByType(@PathVariable @NotNull CatalogType type) {
        return ApiResponse.success(messageResolver.resolve("common.success"), catalogQueryService.getByType(type));
    }

    @GetMapping(ApiPaths.Reference.BY_TYPE_AND_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CatalogResponse>> getByTypeAndParentCode(
            @PathVariable @NotNull CatalogType type,
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                catalogQueryService.getByTypeAndParentCode(type, parentCode)
        );
    }

    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CatalogResponse> create(@Valid @RequestBody CatalogCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), catalogCommandService.create(request));
    }

    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CatalogResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CatalogUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), catalogCommandService.update(id, request));
    }

    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        catalogCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
