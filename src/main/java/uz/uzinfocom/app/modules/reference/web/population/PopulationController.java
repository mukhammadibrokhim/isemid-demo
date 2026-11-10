package uz.uzinfocom.app.modules.reference.web.population;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.reference.application.population.command.PopulationCommandService;
import uz.uzinfocom.app.modules.reference.application.population.dto.PopulationCreateRequest;
import uz.uzinfocom.app.modules.reference.application.population.dto.PopulationUpdateRequest;
import uz.uzinfocom.app.modules.reference.application.population.query.PopulationQueryService;
import uz.uzinfocom.app.modules.reference.application.population.query.dto.PopulationDetailResponse;
import uz.uzinfocom.app.modules.reference.application.population.query.dto.PopulationNodeResponse;
import uz.uzinfocom.app.modules.reference.application.population.sync.PopulationSyncService;
import uz.uzinfocom.app.modules.reference.application.population.sync.dto.PopulationSyncResult;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.util.List;

/**
 * Population reference — <b>super-admin only</b>. The list endpoint is a
 * year-scoped republic → region → district drill-down (the reference table
 * itself), {@code /{id}} is the full detail with localized territory names,
 * the territory's whole year series and the create/update audit stamps, and
 * {@code /sync} refreshes from the stat.uz SDMX feed. Reports read {@code
 * ref_population} through the repository, not this controller, so locking it
 * to super-admin does not affect per-capita report figures.
 */
@Tag(
        name = "Reference - Population",
        description = "API справочника «Численность постоянного населения» (МХОБТ/СОАТО × год), только для "
                + "супер-администратора. Иерархия республика → область → район за выбранный год, детальный "
                + "просмотр с наименованиями по локали и журналом аудита, ручная правка и синхронизация "
                + "из фида stat.uz SDMX (набор 246)."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.POPULATIONS)
@RequiredArgsConstructor
public class PopulationController {

    private final PopulationQueryService populationQueryService;
    private final PopulationCommandService populationCommandService;
    private final PopulationSyncService populationSyncService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Иерархия численности населения за год",
            description = "Без regionCode — строка по республике и все области за выбранный год. С regionCode — "
                    + "районы указанной области за тот же год. Наименования территорий — по языку текущей "
                    + "локали. year по умолчанию — последний загруженный год."
    )
    @GetMapping
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<List<PopulationNodeResponse>> hierarchy(
            @Parameter(description = "Год. По умолчанию — последний загруженный.")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Код области (ref_region.code) для перехода к её районам.")
            @RequestParam(required = false) @Size(max = 50) String regionCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                populationQueryService.hierarchy(regionCode, year)
        );
    }

    @Operation(summary = "Список доступных годов", description = "Годы, за которые есть данные, по убыванию.")
    @GetMapping(ApiPaths.Reference.YEARS)
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<List<Integer>> availableYears() {
        return ApiResponse.success(messageResolver.resolve("common.success"), populationQueryService.availableYears());
    }

    @Operation(
            summary = "Детальный просмотр записи",
            description = "Полная запись по идентификатору: наименования территорий по локали, все годы "
                    + "этой территории (years[]) и аудит создания/изменения (кто и когда)."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<PopulationDetailResponse> getById(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), populationQueryService.getById(id));
    }

    @Operation(summary = "Создать запись (ручной ввод)", description = "Источник записи — MANUAL. (soatoId, year) уникальны.")
    @PostMapping
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<PopulationDetailResponse> create(@Valid @RequestBody PopulationCreateRequest request) {
        return ApiResponse.success(messageResolver.resolve("common.created"), populationCommandService.create(request));
    }

    @Operation(summary = "Обновить запись", description = "Территория и год не изменяются. Изменение фиксируется в журнале аудита.")
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<PopulationDetailResponse> update(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @Valid @RequestBody PopulationUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), populationCommandService.update(id, request));
    }

    @Operation(summary = "Удалить запись", description = "Мягкое удаление. SDMX-синхронизация может вернуть запись обратно.")
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор записи.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        populationCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }

    @Operation(
            summary = "Синхронизировать справочник из stat.uz SDMX",
            description = "Загружает фид и выполняет upsert по (soatoId, year) для годов >= min-year. Записи "
                    + "с источником MANUAL не перезаписываются. SDMX-изменения в журнал аудита не пишутся."
    )
    @PostMapping(ApiPaths.Reference.SYNC)
    @PreAuthorize("@adminAccessGuard.isSuperAdmin()")
    public ApiResponse<PopulationSyncResult> sync() {
        return ApiResponse.success(
                messageResolver.resolve("reference.population.sync.success"), populationSyncService.sync()
        );
    }
}
