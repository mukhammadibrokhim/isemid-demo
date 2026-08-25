package uz.uzinfocom.app.integration.lis.reference.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.lis.reference.application.LisReferenceQueryService;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisCategoryResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisConditionResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisItemTypeResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisOrganizationResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisProfessionResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisReferencePage;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisResearchTypeResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.util.List;

/**
 * Thin pass-through onto {@link LisReferenceQueryService} — dropdown data
 * for filling in an act (institution, storage/special conditions,
 * sampler/participant profession, sample item research
 * type/category/item type). See {@code docs/act-lis-frontend-guide.md} for
 * the field-by-field mapping onto {@code Act153}/{@code Act154}/
 * {@code Act223} and why this proxies LIS instead of the frontend calling it
 * directly. Never called by {@code ACT156}/{@code ACT224} flows — those act
 * types have no LIS counterpart at all (see {@code LisResearchCode}).
 */
@Tag(name = "LIS Reference", description = "Прокси-справочники LIS (организации, профессии, виды исследований и т.д.) для заполнения акта.")
@RestController
@RequestMapping(ApiPaths.LisReference.ROOT)
@RequiredArgsConstructor
public class LisReferenceController {

    private final LisReferenceQueryService queryService;

    @Operation(summary = "Организации LIS", description = "Необязательный фильтр по названию — поиск выполняет сама LIS.")
    @GetMapping(ApiPaths.LisReference.ORGANIZATIONS)
    @PreAuthorize("isAuthenticated()")
    public List<LisOrganizationResponse> organizations(
            @Parameter(description = "Фрагмент названия организации.") @RequestParam(required = false) String name
    ) {
        return queryService.organizations(name);
    }

    @Operation(summary = "Подразделения одной организации LIS")
    @GetMapping(ApiPaths.LisReference.DEPARTMENTS)
    @PreAuthorize("isAuthenticated()")
    public List<LisOrganizationResponse> departments(
            @Parameter(description = "Идентификатор организации в LIS.", required = true)
            @PathVariable Long organizationId
    ) {
        return queryService.departments(organizationId);
    }

    @Operation(summary = "Справочник условий хранения/доставки/особых условий")
    @GetMapping(ApiPaths.LisReference.CONDITIONS)
    @PreAuthorize("isAuthenticated()")
    public List<LisConditionResponse> conditions() {
        return queryService.conditions();
    }

    @Operation(summary = "Справочник профессий", description = "Постраничный список — задайте search для поиска по названию.")
    @GetMapping(ApiPaths.LisReference.PROFESSIONS)
    @PreAuthorize("isAuthenticated()")
    public LisReferencePage<LisProfessionResponse> professions(
            @Parameter(description = "Фрагмент названия профессии.") @RequestParam(required = false) String search,
            @Parameter(description = "Номер страницы, начиная с 0.") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы, максимум 200.") @RequestParam(defaultValue = "50") int limit
    ) {
        return queryService.professions(search, page, limit);
    }

    @Operation(summary = "Справочник видов исследований", description = "Постраничный список — задайте search для поиска по названию.")
    @GetMapping(ApiPaths.LisReference.RESEARCH_TYPES)
    @PreAuthorize("isAuthenticated()")
    public LisReferencePage<LisResearchTypeResponse> researchTypes(
            @Parameter(description = "Фрагмент названия вида исследования.") @RequestParam(required = false) String search,
            @Parameter(description = "Номер страницы, начиная с 0.") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы, максимум 200.") @RequestParam(defaultValue = "50") int limit
    ) {
        return queryService.researchTypes(search, page, limit);
    }

    @Operation(summary = "Справочник категорий объектов исследования", description = "Постраничный список — задайте search для поиска по названию.")
    @GetMapping(ApiPaths.LisReference.CATEGORIES)
    @PreAuthorize("isAuthenticated()")
    public LisReferencePage<LisCategoryResponse> categories(
            @Parameter(description = "Фрагмент названия категории.") @RequestParam(required = false) String search,
            @Parameter(description = "Номер страницы, начиная с 0.") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы, максимум 200.") @RequestParam(defaultValue = "50") int limit
    ) {
        return queryService.categories(search, page, limit);
    }

    @Operation(summary = "Справочник типов объектов исследования", description = "Постраничный список — задайте search для поиска по названию.")
    @GetMapping(ApiPaths.LisReference.ITEM_TYPES)
    @PreAuthorize("isAuthenticated()")
    public LisReferencePage<LisItemTypeResponse> itemTypes(
            @Parameter(description = "Фрагмент названия типа объекта.") @RequestParam(required = false) String search,
            @Parameter(description = "Номер страницы, начиная с 0.") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы, максимум 200.") @RequestParam(defaultValue = "50") int limit
    ) {
        return queryService.itemTypes(search, page, limit);
    }
}
