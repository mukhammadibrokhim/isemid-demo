package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.CatalogRequest;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponse;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapper;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;

import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Catalog.ROOT)
@Tag(name = "Catalog", description = "Работа с каталогом данных")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;
    private final MessageUtil messageUtil;
    private final CatalogMapper catalogMapper;

    @Operation(summary = "Создать новый каталог", description = "Создает новый каталог с предоставленными данными.")
    @PostMapping
    public ApiResponse<CatalogResponse> createCatalog(@Valid @RequestBody CatalogRequest request) {
        return ApiResponse.success(messageUtil, catalogMapper.toDto(catalogService.create(request)));
    }

    @Operation(
            summary = "Получить все каталоги",
            description = """
                    Получает список всех каталогов с возможностью фильтрации по различным параметрам:
                    - type (тип каталога),
                    - code (код каталога),
                    - parentCode (родительский код),
                    - nameRu (название на русском),
                    - nameUz (название на узбекском),
                    - nameUzCyril (название на узбекском кириллицей),
                    - nameKaa (название на языке Kaa).
                    Поддерживается пагинация с параметрами page и size.
                    """
    )
    @GetMapping("/all")
    public PagedResponse<?> getAllCatalogs(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String parentCode,
            @RequestParam(required = false) String nameRu,
            @RequestParam(required = false) String nameUz,
            @RequestParam(required = false) String nameUzCyril,
            @RequestParam(required = false) String nameKaa,
            HttpServletRequest request
    ) {
        Page<Catalog> catalogPage = catalogService.filter(page, size, type, code, parentCode, nameRu, nameUz, nameUzCyril, nameKaa);
        return PagedResponse.build(page, catalogPage, getRequestUrl(request), messageUtil);
    }

    @Operation(summary = "Обновить каталог по ID", description = "Обновляет существующий каталог, используя ID каталога и предоставленные данные.")
    @PutMapping("/{id}")
    public ApiResponse<CatalogResponse> update(@PathVariable long id, @RequestBody CatalogRequest catalogRequest) {
        return ApiResponse.success(messageUtil, catalogMapper.toDto(catalogService.update(id, catalogRequest)));
    }

    @Operation(summary = "Получить каталог по ID", description = "Возвращает данные каталога по его уникальному идентификатору.")
    @GetMapping("/{id}")
    public ApiResponse<CatalogResponse> get(@PathVariable long id) {
        return ApiResponse.success(messageUtil, catalogMapper.toDto(catalogService.getById(id)));
    }

    @Operation(summary = "Удалить каталог по ID", description = "Удаляет каталог по его уникальному идентификатору.")
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable long id) {
        catalogService.delete(id);
        return ApiResponse.success(messageUtil, null);
    }
}
