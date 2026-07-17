package uz.uzinfocom.app.platform.reference.web.district;

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
import uz.uzinfocom.app.platform.reference.application.district.dto.DistrictCreateRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictFilterRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictTableResponse;
import uz.uzinfocom.app.platform.reference.application.district.dto.DistrictUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.district.command.DistrictCommandService;
import uz.uzinfocom.app.platform.reference.application.district.query.DistrictQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.util.List;

@Tag(
        name = "Reference - Districts",
        description = "API для управления справочником районов."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.DISTRICTS)
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictQueryService districtQueryService;
    private final DistrictCommandService districtCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить постраничные данные справочника районов",
            description = """
                    Возвращает активные записи справочника районов в виде постраничной таблицы.

                    Поддерживаемые фильтры: code, name, soatoId.
                    District.parentCode указывает на код родительского региона.
                    Нумерация страниц начинается с 1.
                    Поддерживаемые поля сортировки: id, code, parentCode, soatoId, parentSoatoId, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Районы успешно получены."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<DistrictTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute DistrictFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<DistrictTableResponse> page = districtQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить район по идентификатору",
            description = "Возвращает одну активную запись района по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Район успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DistrictResponse> getById(
            @Parameter(description = "Внутренний идентификатор района.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), districtQueryService.getById(id));
    }

    @Operation(
            summary = "Получить район по коду",
            description = "Возвращает одну активную запись района по нормализованному коду района."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Район успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DistrictResponse> getByCode(
            @Parameter(description = "Код района.", required = true, example = "AN-202")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), districtQueryService.getByCode(code));
    }

    @Operation(
            summary = "Получить районы по коду региона",
            description = "Возвращает активные записи районов, у которых parentCode совпадает с указанным кодом региона."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Районы успешно получены."
    )
    @GetMapping(ApiPaths.Reference.BY_PARENT_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<DistrictResponse>> getByParentCode(
            @Parameter(description = "Код региона, хранящийся в District.parentCode.", required = true, example = "UZ-AN")
            @PathVariable @NotBlank @Size(max = 50) String parentCode
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                districtQueryService.getByParentCode(parentCode)
        );
    }

    @Operation(
            summary = "Создать район",
            description = "Создаёт новую запись района в составе существующего родительского региона."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Район успешно создан."
    )
    @PostMapping
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<DistrictResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемого района.",
                    required = true
            )
            @Valid @RequestBody DistrictCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), districtCommandService.create(request));
    }

    @Operation(
            summary = "Обновить район",
            description = "Обновляет существующую активную запись района по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Район успешно обновлён."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<DistrictResponse> update(
            @Parameter(description = "Внутренний идентификатор района.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные района.",
                    required = true
            )
            @Valid @RequestBody DistrictUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), districtCommandService.update(id, request));
    }

    @Operation(
            summary = "Удалить район",
            description = "Мягко удаляет запись района. Удалённые записи исключаются из эндпоинтов чтения."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Район успешно удалён."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор района.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        districtCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
