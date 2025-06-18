package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.CatalogRequest;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponse;
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

    @Operation(
            summary = "Создать новый каталог",
            description = "Создает новый каталог с предоставленными данными."
    )
    @PostMapping
    public ApiResponse<CatalogResponse> createCatalog(@Valid @RequestBody CatalogRequest request) {
        return catalogService.create(request);
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
                    """,
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы для пагинации"),
                    @Parameter(name = "size", description = "Количество элементов на странице"),
                    @Parameter(name = "type", description = "Фильтр по типу каталога"),
                    @Parameter(name = "code", description = "Фильтр по коду каталога"),
                    @Parameter(name = "parentCode", description = "Фильтр по родительскому коду"),
                    @Parameter(name = "nameRu", description = "Фильтр по названию на русском языке"),
                    @Parameter(name = "nameUz", description = "Фильтр по названию на узбекском языке"),
                    @Parameter(name = "nameUzCyril", description = "Фильтр по названию на узбекском кириллицей"),
                    @Parameter(name = "nameKaa", description = "Фильтр по названию на языке Kaa"),
            }
    )
    @GetMapping("/all")
    public ApiResponsePage getAllCatalogs(
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
        return catalogService.getFilteredCatalogs(page, size, type, code, parentCode, nameRu, nameUz, nameUzCyril, nameKaa, getRequestUrl(request));
    }

    @Operation(
            summary = "Обновить каталог по ID",
            description = "Обновляет существующий каталог, используя ID каталога и предоставленные данные.",
            parameters = {
                    @Parameter(name = "id", description = "ID каталога для обновления", required = true)
            }
    )
    @PutMapping("/{id}")
    public ApiResponse<CatalogResponse> update(@PathVariable long id, @RequestBody CatalogRequest catalogRequest) {
        return catalogService.update(id, catalogRequest);
    }

    @Operation(
            summary = "Получить каталог по ID",
            description = "Возвращает данные каталога по его уникальному идентификатору.",
            parameters = {
                    @Parameter(name = "id", description = "ID каталога", required = true)
            }
    )
    @GetMapping("/{id}")
    public ApiResponse<CatalogResponse> get(@PathVariable long id) {
        return catalogService.getById(id);
    }

    @Operation(
            summary = "Удалить каталог по ID",
            description = "Удаляет каталог по его уникальному идентификатору.",
            parameters = {
                    @Parameter(name = "id", description = "ID каталога для удаления", required = true)
            }
    )
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable long id) {
        return catalogService.delete(id);
    }
}
