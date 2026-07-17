package uz.uzinfocom.app.platform.reference.web.neighborhood;

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
import uz.uzinfocom.app.platform.reference.application.neighborhood.dto.NeighborhoodCreateRequest;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodFilterRequest;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodTableResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.dto.NeighborhoodUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.neighborhood.command.NeighborhoodCommandService;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.NeighborhoodQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.util.List;

@Tag(
        name = "Reference - Neighborhoods",
        description = "API для управления справочником махаллей."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.NEIGHBORHOODS)
@RequiredArgsConstructor
public class NeighborhoodController {

    private final NeighborhoodQueryService neighborhoodQueryService;
    private final NeighborhoodCommandService neighborhoodCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить постраничные данные справочника махаллей",
            description = """
                    Возвращает активные записи справочника махаллей в виде постраничной таблицы.

                    Поддерживаемые фильтры: code, name, soatoId.
                    Neighborhood.parentCode указывает на код родительского района.
                    Нумерация страниц начинается с 1.
                    Поддерживаемые поля сортировки: id, code, parentCode, soatoId, parentSoatoId, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Махалли успешно получены."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<NeighborhoodTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute NeighborhoodFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<NeighborhoodTableResponse> page = neighborhoodQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить махаллю по идентификатору",
            description = "Возвращает одну активную запись махалли по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Махалля успешно получена."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NeighborhoodResponse> getById(
            @Parameter(description = "Внутренний идентификатор махалли.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), neighborhoodQueryService.getById(id));
    }

    @Operation(
            summary = "Получить махаллю по коду",
            description = "Возвращает одну активную запись махалли по нормализованному коду махалли."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Махалля успешно получена."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NeighborhoodResponse> getByCode(
            @Parameter(description = "Код махалли.", required = true, example = "AN-202001")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), neighborhoodQueryService.getByCode(code));
    }

    @Operation(
            summary = "Получить махалли по коду района",
            description = "Возвращает активные записи махаллей, у которых parentCode совпадает с указанным кодом района."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Махалли успешно получены."
    )
    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<NeighborhoodResponse>> getByParentCode(
            @Parameter(description = "Код района, хранящийся в Neighborhood.parentCode.", required = true, example = "AN-202")
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                neighborhoodQueryService.getByParentCode(parentCode)
        );
    }

    @Operation(
            summary = "Создать махаллю",
            description = "Создаёт новую запись махалли в составе существующего родительского района."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Махалля успешно создана."
    )
    @PostMapping
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<NeighborhoodResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемой махалли.",
                    required = true
            )
            @Valid @RequestBody NeighborhoodCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), neighborhoodCommandService.create(request));
    }

    @Operation(
            summary = "Обновить махаллю",
            description = "Обновляет существующую активную запись махалли по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Махалля успешно обновлена."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<NeighborhoodResponse> update(
            @Parameter(description = "Внутренний идентификатор махалли.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные махалли.",
                    required = true
            )
            @Valid @RequestBody NeighborhoodUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), neighborhoodCommandService.update(id, request));
    }

    @Operation(
            summary = "Удалить махаллю",
            description = "Мягко удаляет запись махалли. Удалённые записи исключаются из эндпоинтов чтения."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Махалля успешно удалена."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор махалли.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        neighborhoodCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
