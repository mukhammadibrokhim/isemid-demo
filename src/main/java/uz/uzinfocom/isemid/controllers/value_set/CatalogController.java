package uz.uzinfocom.isemid.controllers.value_set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.ReqCatalog;
import uz.uzinfocom.isemid.models.payload.responses.RespCatalog;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.List;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.CATALOG;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;

@RestController
@RequestMapping(CATALOG)
@Tag(name = "Catalog", description = "Работа с каталогом данных")
@RequiredArgsConstructor
public class CatalogController {

    final CatalogService catalogService;

    //    @PreAuthorize("hasAnyAuthority('create_catalog')")
    @PostMapping
    public ApiResponse<Catalog> createCatalog(@Valid @RequestBody ReqCatalog reqCatalog) {
        return catalogService.createCatalog(reqCatalog);
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
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String parentCode,
            @RequestParam(required = false) String nameRu,
            @RequestParam(required = false) String nameUz,
            @RequestParam(required = false) String nameUzCyril,
            @RequestParam(required = false) String nameKaa,
            HttpServletRequest request
    ) {
        return catalogService.getAllCatalogs(page, size, type, code, parentCode, nameRu, nameUz, nameUzCyril, nameKaa, request);
    }

    @Operation(
            summary = "Получить все типы каталогов",
            description = "Получает список всех типов каталогов, доступных в системе.")
    @GetMapping("/types")
    public ApiResponse<List<RespCatalog>> getCatalogs() {
        String lang = "";
        return catalogService.findAllByType(CatalogType.catalogType, lang);
    }

    @Operation(
            summary = "Обновить каталог",
            description = "Обновляет существующий каталог по его ID. Передайте данные каталога для обновления.",
            parameters = {
                    @Parameter(name = "id", description = "ID каталога для обновления", required = true)})
    @PutMapping("/{id}")
    public ApiResponse<Catalog> updateCatalog(@PathVariable long id, @RequestBody ReqCatalog reqCatalog) {
        return catalogService.updateCatalogById(id, reqCatalog);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteCatalog(@PathVariable long id) {
        return catalogService.deleteById(id);
    }

    @Operation(
            summary = "Получить все состояния",
            description = "Получает список всех доступных состояний из каталога.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/state")
    public ApiResponse<List<RespCatalog>> getCatalogState(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.state, lang);
    }

    @Operation(
            summary = "Получить все города по родительскому коду",
            description = "Получает список всех городов на основе родительского кода для фильтрации.",
            parameters = {
                    @Parameter(name = "parentCode", description = "Родительский код для фильтрации городов", required = true),
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/city")
    public ApiResponse<List<RespCatalog>> getCatalogCity(
            @RequestParam String parentCode,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByTypeAndParentCode(CatalogType.city, parentCode, lang);
    }

    @Operation(
            summary = "Получить все типы идентификаторов",
            description = "Получает список всех доступных типов идентификаторов.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/identifier-type")
    public ApiResponse<List<RespCatalog>> getIdentifierType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.identifierType, lang);
    }

    @Operation(
            summary = "Получить все полы",
            description = "Получает список всех доступных полов.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/gender")
    public ApiResponse<List<RespCatalog>> getGender(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.gender, lang);
    }

    @Operation(
            summary = "Получить все страны",
            description = "Получает список всех стран.",
            parameters = {
                    @Parameter(name = "search", description = "Ключевое слово для фильтрации стран."),
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/country")
    public ApiResponse<List<RespCatalog>> getCountry(
            @RequestHeader("Accept-Language") String lang,
            @RequestParam(value = "search", required = false) String search

    ) {
        return catalogService.findFirst20ByTypeAndSearch(CatalogType.country, lang, search);
    }

    @Operation(
            summary = "Получить национальности по родительскому ID",
            description = "Получает список национальностей.",
            parameters = {
                    @Parameter(name = "search", description = "Ключевое слово для фильтрации национальности."),
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/nationality")
    public ApiResponse<List<RespCatalog>> getNationality(
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findFirst20ByTypeAndSearch(CatalogType.nationality, lang, search);
    }

    @Operation(
            summary = "Получить все семейные положения",
            description = "Возвращает список всех типов семейных положений, включая их наименования на русском, узбекском (латиница и кириллица) языках и другие связанные данные.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/marital-status")
    public ApiResponse<List<RespCatalog>> getMaritalStatus(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.maritalStatus, lang);
    }

    @Operation(
            summary = "Получить все статусы проживания",
            description = "Возвращает список всех типов статусов проживания, включая их наименования на русском, узбекском (латиница и кириллица) языках и другие связанные данные.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/residential-status")
    public ApiResponse<List<RespCatalog>> getResidentialStatus(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.residentialStatus, lang);
    }

    @Operation(
            summary = "Получить тип адреса",
            description = "Возвращает список всех типов адресов, включая их названия на русском, узбекском (латиница и кириллица), а также каракалпакском языках.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/address-type")
    public ApiResponse<List<RespCatalog>> getAddressType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.addressType, lang);
    }

    @GetMapping("/population-type")
    @Operation(
            summary = "Получить тип населённого пункта",
            description = "Возвращает список всех типов населённых пунктов, включая их названия на русском, узбекском (латиница и кириллица), а также каракалпакском языках.",
            parameters = {
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)})
    public ApiResponse<List<RespCatalog>> getPopulationType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.populationType, lang);
    }

    @GetMapping("/positions")
    public ApiResponse<List<RespCatalog>> getPositions(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.position, lang);
    }

    @GetMapping("/levels")
    public ApiResponse<?> getLevels(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.level, lang);
    }

    @GetMapping("/forms")
    public ApiResponse<?> getCards(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.formType, lang);
    }

    @GetMapping("/disease-place")
    public ApiResponse<List<RespCatalog>> getDeceasePlace(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.diseasePlace, lang);
    }

    @GetMapping("/category")
    public ApiResponse<?> getCategory(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.category, lang);
    }

    @GetMapping("/profession")
    public ApiResponse<List<RespCatalog>> getProfession(
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findByProfessionTypeAndSearch(CatalogType.profession, lang, search);
    }

    @GetMapping("/neighborhood")
    public ApiResponse<List<RespCatalog>> getCatalogNeighborhood(
            @RequestParam String parentCode,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByTypeAndParentCode(CatalogType.neighborhood, parentCode, lang);

    }
}
