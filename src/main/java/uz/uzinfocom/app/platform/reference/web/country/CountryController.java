package uz.uzinfocom.app.platform.reference.web.country;

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
import uz.uzinfocom.app.platform.reference.application.country.dto.CountryCreateRequest;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryFilterRequest;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryDetailedResponse;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryTableResponse;
import uz.uzinfocom.app.platform.reference.application.country.dto.CountryUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.country.command.CountryCommandService;
import uz.uzinfocom.app.platform.reference.application.country.query.CountryQueryService;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Tag(
        name = "Reference - Countries",
        description = "API для управления справочником стран."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.COUNTRIES)
@RequiredArgsConstructor
public class CountryController {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final CountryQueryService countryQueryService;
    private final CountryCommandService countryCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить постраничные данные справочника стран",
            description = """
                    Возвращает активные записи справочника стран в виде постраничной таблицы.

                    Поддерживаемые фильтры: code, name.
                    Нумерация страниц начинается с 1.
                    Поддерживаемые поля сортировки: id, code, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Страны успешно получены."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<CountryTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute CountryFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<CountryTableResponse> page = countryQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить страну по идентификатору",
            description = "Возвращает одну активную запись страны по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Страна успешно получена."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CountryDetailedResponse> getById(
            @Parameter(description = "Внутренний идентификатор страны.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), countryQueryService.getById(id));
    }

    @Operation(
            summary = "Получить страну по коду",
            description = "Возвращает одну активную запись страны по нормализованному коду страны."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Страна успешно получена."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CountryDetailedResponse> getByCode(
            @Parameter(description = "Код страны.", required = true, example = "UZB")
            @PathVariable @NotBlank @Size(max = 50) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), countryQueryService.getByCode(code));
    }

    @Operation(
            summary = "Создать страну",
            description = "Создаёт новую запись страны. Коды нормализуются перед сохранением."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Страна успешно создана."
    )
    @PostMapping
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CountryDetailedResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемой страны.",
                    required = true
            )
            @Valid @RequestBody CountryCreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), countryCommandService.create(request));
    }

    @Operation(
            summary = "Обновить страну",
            description = "Обновляет существующую активную запись страны по внутреннему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Страна успешно обновлена."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<CountryDetailedResponse> update(
            @Parameter(description = "Внутренний идентификатор страны.", required = true, example = "1")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные страны.",
                    required = true
            )
            @Valid @RequestBody CountryUpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), countryCommandService.update(id, request));
    }

    @Operation(
            summary = "Удалить страну",
            description = "Мягко удаляет запись страны. Удалённые записи исключаются из эндпоинтов чтения."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Страна успешно удалена."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "Внутренний идентификатор страны.", required = true, example = "1")
            @PathVariable @Positive Long id
    ) {
        countryCommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
