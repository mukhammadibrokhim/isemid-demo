package uz.uzinfocom.app.modules.reference.web.icd10;

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
import uz.uzinfocom.app.modules.reference.application.icd10.command.Icd10CommandService;
import uz.uzinfocom.app.modules.reference.application.icd10.dto.Icd10CreateRequest;
import uz.uzinfocom.app.modules.reference.application.icd10.dto.Icd10UpdateRequest;
import uz.uzinfocom.app.modules.reference.application.icd10.query.Icd10QueryService;
import uz.uzinfocom.app.modules.reference.application.icd10.query.dto.Icd10FilterRequest;
import uz.uzinfocom.app.modules.reference.application.icd10.query.dto.Icd10Response;
import uz.uzinfocom.app.modules.reference.application.icd10.query.dto.Icd10TableResponse;
import uz.uzinfocom.app.modules.reference.application.icd10.query.dto.Icd10TreeResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.util.List;

@Tag(
        name = "Reference - MKB-10",
        description = "API для управления справочником классификатора МКБ-10. Узлы массово импортируются из " +
                "внешней иерархии МКБ-10 ВОЗ и сохраняют идентификаторы источника для целостности связей " +
                "родитель-потомок."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Reference.ICD10)
@RequiredArgsConstructor
public class Icd10Controller {

    private final Icd10QueryService icd10QueryService;
    private final Icd10CommandService icd10CommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить постраничные данные классификатора МКБ-10",
            description = """
                    Возвращает активные узлы МКБ-10 в виде постраничной таблицы.

                    Поддерживаемые фильтры: code, name, parentId, level.
                    Нумерация страниц начинается с 1.
                    Поддерживаемые поля сортировки: id, code, level, nameUz, nameUzCyril, nameRu, nameKaa, createdAt, updatedAt.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Узлы МКБ-10 успешно получены."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Icd10TableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute Icd10FilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<Icd10TableResponse> page = icd10QueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить корневые разделы МКБ-10",
            description = "Возвращает все активные узлы МКБ-10 без родителя (верхний уровень дерева классификатора). " +
                    "Наименование возвращается одним полем `name`, уже разрешённым под локаль запроса (заголовок Accept-Language)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Корневые узлы МКБ-10 успешно получены."
    )
    @GetMapping(ApiPaths.Reference.ROOTS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Icd10TreeResponse>> getRoots() {
        return ApiResponse.success(messageResolver.resolve("common.success"), icd10QueryService.getRoots());
    }

    @Operation(
            summary = "Получить дочерние узлы МКБ-10",
            description = "Возвращает все активные узлы МКБ-10, чей родитель — указанный идентификатор узла, для навигации по дереву. " +
                    "Наименование возвращается одним полем `name`, уже разрешённым под локаль запроса (заголовок Accept-Language)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Дочерние узлы МКБ-10 успешно получены."
    )
    @GetMapping(ApiPaths.Reference.CHILDREN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Icd10TreeResponse>> getChildren(
            @Parameter(description = "Внешний идентификатор родительского узла МКБ-10.", required = true, example = "12")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), icd10QueryService.getChildren(id));
    }

    @Operation(
            summary = "Получить узел МКБ-10 по идентификатору",
            description = "Возвращает один активный узел МКБ-10 по его внешнему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Узел МКБ-10 успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Icd10Response> getById(
            @Parameter(description = "Внешний идентификатор узла МКБ-10.", required = true, example = "1500")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), icd10QueryService.getById(id));
    }

    @Operation(
            summary = "Получить узел МКБ-10 по коду",
            description = "Возвращает один активный узел МКБ-10 по его нормализованному коду МКБ-10. " +
                    "Наименование возвращается одним полем `name`, уже разрешённым под локаль запроса (заголовок Accept-Language)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Узел МКБ-10 успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Icd10TreeResponse> getByCode(
            @Parameter(description = "Код МКБ-10.", required = true, example = "A15")
            @PathVariable @NotBlank @Size(max = 20) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), icd10QueryService.getByCode(code));
    }

    @Operation(
            summary = "Создать узел МКБ-10",
            description = "Создаёт новый узел классификатора МКБ-10. Идентификатор должен соответствовать " +
                    "нумерации внешнего источника МКБ-10 ВОЗ, чтобы связи родитель-потомок оставались корректными " +
                    "при повторном импорте."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Узел МКБ-10 успешно создан."
    )
    @PostMapping
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Icd10Response> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемого узла МКБ-10.",
                    required = true
            )
            @Valid @RequestBody Icd10CreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), icd10CommandService.create(request));
    }

    @Operation(
            summary = "Обновить узел МКБ-10",
            description = "Обновляет существующий активный узел МКБ-10 по его внешнему идентификатору."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Узел МКБ-10 успешно обновлён."
    )
    @PutMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Icd10Response> update(
            @Parameter(description = "Внешний идентификатор узла МКБ-10.", required = true, example = "1500")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные узла МКБ-10.",
                    required = true
            )
            @Valid @RequestBody Icd10UpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), icd10CommandService.update(id, request));
    }

    @Operation(
            summary = "Удалить узел МКБ-10",
            description = "Мягко удаляет узел МКБ-10. Узлы с активными дочерними элементами удалить нельзя."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Узел МКБ-10 успешно удалён."
    )
    @DeleteMapping(ApiPaths.Reference.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Внешний идентификатор узла МКБ-10.", required = true, example = "1500")
            @PathVariable @Positive Long id
    ) {
        icd10CommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
