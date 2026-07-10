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
        description = "API для управления справочником регионов."
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
            summary = "Получить постраничные данные справочника регионов",
            description = """
                    Возвращает активные записи справочника регионов в виде постраничной таблицы.

                    Поддерживаемые фильтры: code, name, soatoId.
                    Region.parentCode указывает на код родительской страны.
                    Нумерация страниц начинается с 1.
                    Поддерживаемые поля сортировки: id, code, parentCode, soatoId, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Регионы успешно получены."
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
            summary = "Получить регион по идентификатору",
            description = "Возвращает одну активную запись региона по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Регион успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RegionResponse> getById(
            @Parameter(description = "Внутренний идентификатор региона.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), regionQueryService.getById(id));
    }

    @Operation(
            summary = "Получить регион по коду",
            description = "Возвращает одну активную запись региона по нормализованному коду региона."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Регион успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RegionResponse> getByCode(
            @Parameter(description = "Код региона.", required = true, example = "UZ-AN")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), regionQueryService.getByCode(code));
    }

    @Operation(
            summary = "Получить регионы по коду страны",
            description = "Возвращает активные записи регионов, у которых parentCode совпадает с указанным кодом страны."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Регионы успешно получены."
    )
    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<RegionResponse>> getByParentCode(
            @Parameter(description = "Код страны, хранящийся в Region.parentCode.", required = true, example = "UZ")
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                regionQueryService.getByParentCode(parentCode)
        );
    }

    @Operation(
            summary = "Создать регион",
            description = "Создаёт новую запись региона в составе существующей родительской страны."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Регион успешно создан."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<RegionResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемого региона.",
                    required = true
            )
            @Valid @RequestBody RegionCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), regionCommandService.create(request));
    }

    @Operation(
            summary = "Обновить регион",
            description = "Обновляет существующую активную запись региона по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Регион успешно обновлён."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<RegionResponse> update(
            @Parameter(description = "Внутренний идентификатор региона.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные региона.",
                    required = true
            )
            @Valid @RequestBody RegionUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), regionCommandService.update(id, request));
    }

    @Operation(
            summary = "Удалить регион",
            description = "Мягко удаляет запись региона. Удалённые записи исключаются из эндпоинтов чтения."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Регион успешно удалён."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор региона.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        regionCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
