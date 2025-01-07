package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Catalog;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.payload.requests.ReqCatalog;
import uz.uzinfocom.isemid.models.payload.responses.RespCatalog;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.CatalogService;

import java.util.List;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.CATALOG;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;

@RestController
@RequestMapping(CATALOG)
@Tag(name = "Catalog")
@RequiredArgsConstructor
public class CatalogController {

    final CatalogService catalogService;

    @PostMapping
    public ApiResponse<Catalog> createCatalog(@Valid @RequestBody ReqCatalog reqCatalog) {
        return catalogService.createCatalog(reqCatalog);
    }

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

    @GetMapping("/types")
    public ApiResponse<List<RespCatalog>> getCatalogs(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.catalogType, lang);
    }

    @PutMapping("/{id}")
    public ApiResponse<Catalog> updateCatalog(@PathVariable long id, @RequestBody ReqCatalog reqCatalog) {
        return catalogService.updateCatalogById(id, reqCatalog);
    }

    @GetMapping("/state")
    public ApiResponse<List<RespCatalog>> getCatalogState(@RequestHeader("Accept-Language") String lang) {
        return catalogService.findAllByType(CatalogType.state, lang);
    }

    @GetMapping("/city")
    public ApiResponse<List<RespCatalog>> getCatalogCity(
            @RequestParam String parentCode,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByTypeAndParentCode(CatalogType.city, parentCode, lang);
    }

    @Operation(summary = "Get all identifier types", description = "Fetches a list of all available identifier types.", parameters = {
            @Parameter(name = "Accept-Language", description = "Language code for localization (e.g., 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/identifier-type")
    public ApiResponse<List<RespCatalog>> getIdentifierType(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.identifierType, lang);
    }

    @Operation(summary = "Get all genders", description = "Fetches a list of all genders.", parameters = {
            @Parameter(name = "Accept-Language", description = "Language code for localization (e.g., 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/gender")
    public ApiResponse<List<RespCatalog>> getGender(
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findAllByType(CatalogType.gender, lang);
    }


    @Operation(summary = "Get all countries", description = "Fetches a list of all countries.", parameters = {
            @Parameter(name = "search", description = "Search keyword for filtering countries."),
            @Parameter(name = "Accept-Language", description = "Language code for localization (e.g., 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/country")
    public ApiResponse<List<RespCatalog>> getCountry(
            @RequestHeader("Accept-Language") String lang,
            @RequestParam(value = "search", required = false) String search

    ) {
        return catalogService.findFirst20ByTypeAndSearch(CatalogType.country, lang, search);
    }

    @Operation(summary = "Get nationalities by parent ID", description = "Fetches a list of nationalities", parameters = {
            @Parameter(name = "search", description = "Search keyword for filtering nationality."),
            @Parameter(name = "Accept-Language", description = "Language code for localization (e.g., 'ru', 'uz', 'uzCyril')", required = true)})
    @GetMapping("/nationality")
    public ApiResponse<List<RespCatalog>> getNationality(
            @RequestParam(value = "search", required = false) String search,
            @RequestHeader("Accept-Language") String lang
    ) {
        return catalogService.findFirst20ByTypeAndSearch(CatalogType.nationality, lang, search);
    }
}
