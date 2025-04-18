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

    @PostMapping
    public ApiResponse<CatalogResponse> createCatalog(@Valid @RequestBody CatalogRequest request) {
        return catalogService.create(request);
    }

    @Operation(
            summary = "Получить все каталоги",
            description = """
                    Получает список всех каталогов с возможностью фильтрации:
                    - Параметры фильтрации включают тип, код, родительский код, и различные названия (например, на русском, узбекском, кириллицей).
                    - Пагинация доступна с параметрами `page` и `size`.
                    """,
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы для пагинации"),
                    @Parameter(name = "size", description = "Количество элементов на странице для пагинации"),
                    @Parameter(name = "type", description = "Тип каталога для фильтрации"),
                    @Parameter(name = "code", description = "Код каталога для фильтрации"),
                    @Parameter(name = "parentCode", description = "Родительский код для фильтрации"),
                    @Parameter(name = "nameRu", description = "Название на русском языке для фильтрации"),
                    @Parameter(name = "nameUz", description = "Название на узбекском языке для фильтрации"),
                    @Parameter(name = "nameUzCyril", description = "Название на узбекском языке (кириллицей) для фильтрации"),
                    @Parameter(name = "nameKaa", description = "Название на языке Kaa для фильтрации"),
            })
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
            summary = "Обновить каталог",
            description = "Обновляет существующий каталог по его ID. Передайте данные каталога для обновления.",
            parameters = {
                    @Parameter(name = "id", description = "ID каталога для обновления", required = true)})
    @PutMapping("/{id}")
    public ApiResponse<CatalogResponse> update(@PathVariable long id, @RequestBody CatalogRequest catalogRequest) {
        return catalogService.update(id, catalogRequest);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable long id) {
        return catalogService.delete(id);
    }
}
