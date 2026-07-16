package uz.uzinfocom.app.platform.reference.web.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.reference.application.catalog.command.CatalogCommandService;
import uz.uzinfocom.app.platform.reference.application.catalog.dto.CatalogCreateRequest;
import uz.uzinfocom.app.platform.reference.application.catalog.dto.CatalogUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.catalog.query.CatalogQueryService;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogFilterRequest;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogTableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.util.List;

@Tag(
        name = "Reference - Catalogs",
        description = "API для управления общими справочниками-каталогами."
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
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить постраничные данные каталога",
            description = """
                    Возвращает активные записи справочника-каталога в виде постраничной таблицы.

                    Поддерживаемые фильтры: type, code, parentCode, search.
                    Catalog.parentCode указывает на другой элемент того же типа каталога.
                    Нумерация страниц начинается с 1.
                    Поддерживаемые поля сортировки: id, type, code, parentCode, nameUz, nameRu.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Элементы каталога успешно получены."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<CatalogTableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute CatalogFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<CatalogTableResponse> page = catalogQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить элемент каталога по идентификатору",
            description = "Возвращает один активный элемент каталога по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Элемент каталога успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CatalogResponse> getById(
            @Parameter(description = "Внутренний идентификатор элемента каталога.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), catalogQueryService.getById(id));
    }

    @Operation(
            summary = "Получить элемент каталога по типу и коду",
            description = "Возвращает один активный элемент каталога по типу каталога и нормализованному коду элемента."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Элемент каталога успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_TYPE_AND_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CatalogResponse> getByTypeAndCode(
            @Parameter(description = "Тип каталога.", required = true, example = "GENDER")
            @PathVariable @NotNull String type,
            @Parameter(description = "Код элемента каталога.", required = true)
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                catalogQueryService.getByTypeAndCode(type, code)
        );
    }

    @Operation(
            summary = "Получить элементы каталога по типу",
            description = "Возвращает активные элементы каталога указанного типа, отсортированные по порядку и наименованию на узбекском языке."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Элементы каталога успешно получены."
    )
    @GetMapping(ApiPaths.Reference.BY_TYPE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CatalogResponse>> getByType(
            @Parameter(description = "Тип каталога.", required = true, example = "GENDER")
            @PathVariable @NotNull String type
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), catalogQueryService.getByType(type));
    }

    @Operation(
            summary = "Получить элементы каталога по типу и коду родителя",
            description = "Возвращает активные элементы каталога указанного типа, у которых parentCode совпадает с другим элементом того же типа каталога."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Элементы каталога успешно получены."
    )
    @GetMapping(ApiPaths.Reference.BY_TYPE_AND_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<CatalogResponse>> getByTypeAndParentCode(
            @Parameter(description = "Тип каталога.", required = true, example = "GENDER")
            @PathVariable @NotNull String type,
            @Parameter(description = "Код родительского элемента того же типа каталога.", required = true)
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                catalogQueryService.getByTypeAndParentCode(type, parentCode)
        );
    }

    @Operation(
            summary = "Создать элемент каталога",
            description = "Создаёт новый элемент каталога. Код и опциональный код родителя нормализуются перед сохранением."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Элемент каталога успешно создан."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CatalogResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемого элемента каталога.",
                    required = true
            )
            @Valid @RequestBody CatalogCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), catalogCommandService.create(request));
    }

    @Operation(
            summary = "Обновить элемент каталога",
            description = "Обновляет существующий активный элемент каталога по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Элемент каталога успешно обновлён."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CatalogResponse> update(
            @Parameter(description = "Внутренний идентификатор элемента каталога.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные элемента каталога.",
                    required = true
            )
            @Valid @RequestBody CatalogUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), catalogCommandService.update(id, request));
    }

    @Operation(
            summary = "Удалить элемент каталога",
            description = "Мягко удаляет элемент каталога. Удалённые записи исключаются из эндпоинтов чтения."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Элемент каталога успешно удалён."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор элемента каталога.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        catalogCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
