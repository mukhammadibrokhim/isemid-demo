package uz.uzinfocom.app.platform.reference.web.mkb10;

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
import uz.uzinfocom.app.platform.reference.application.mkb10.command.Mkb10CommandService;
import uz.uzinfocom.app.platform.reference.application.mkb10.dto.Mkb10CreateRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.dto.Mkb10UpdateRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.Mkb10QueryService;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10FilterRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10Response;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10TableResponse;
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
@RequestMapping(ApiPaths.Reference.MKB10)
@RequiredArgsConstructor
public class Mkb10Controller {

    private static final String ADMIN_AUTHORITIES =
            "hasAnyAuthority('isemid_super_admin', 'isemid_admin', 'ROLE_ISEMID_SUPER_ADMIN', 'ROLE_ISEMID_ADMIN')";

    private final Mkb10QueryService mkb10QueryService;
    private final Mkb10CommandService mkb10CommandService;
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
    public PagedResponse<Mkb10TableResponse> findTable(
            @ParameterObject @Valid @ModelAttribute Mkb10FilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<Mkb10TableResponse> page = mkb10QueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить корневые разделы МКБ-10",
            description = "Возвращает все активные узлы МКБ-10 без родителя (верхний уровень дерева классификатора)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Корневые узлы МКБ-10 успешно получены."
    )
    @GetMapping(ApiPaths.Reference.ROOTS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Mkb10Response>> getRoots() {
        return ApiResponse.success(messageResolver.resolve("common.success"), mkb10QueryService.getRoots());
    }

    @Operation(
            summary = "Получить дочерние узлы МКБ-10",
            description = "Возвращает все активные узлы МКБ-10, чей родитель — указанный идентификатор узла, для навигации по дереву."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Дочерние узлы МКБ-10 успешно получены."
    )
    @GetMapping(ApiPaths.Reference.CHILDREN)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Mkb10Response>> getChildren(
            @Parameter(description = "Внешний идентификатор родительского узла МКБ-10.", required = true, example = "12")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), mkb10QueryService.getChildren(id));
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
    public ApiResponse<Mkb10Response> getById(
            @Parameter(description = "Внешний идентификатор узла МКБ-10.", required = true, example = "1500")
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), mkb10QueryService.getById(id));
    }

    @Operation(
            summary = "Получить узел МКБ-10 по коду",
            description = "Возвращает один активный узел МКБ-10 по его нормализованному коду МКБ-10."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Узел МКБ-10 успешно получен."
    )
    @GetMapping(ApiPaths.Reference.BY_CODE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Mkb10Response> getByCode(
            @Parameter(description = "Код МКБ-10.", required = true, example = "A15")
            @PathVariable @NotBlank @Size(max = 20) String code
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), mkb10QueryService.getByCode(code));
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
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Mkb10Response> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные создаваемого узла МКБ-10.",
                    required = true
            )
            @Valid @RequestBody Mkb10CreateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.created"), mkb10CommandService.create(request));
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
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Mkb10Response> update(
            @Parameter(description = "Внешний идентификатор узла МКБ-10.", required = true, example = "1500")
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные узла МКБ-10.",
                    required = true
            )
            @Valid @RequestBody Mkb10UpdateRequest request
    ) {
        return ApiResponse.success(messageResolver.resolve("common.updated"), mkb10CommandService.update(id, request));
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
    @PreAuthorize(ADMIN_AUTHORITIES)
    public ApiResponse<Void> delete(
            @Parameter(description = "Внешний идентификатор узла МКБ-10.", required = true, example = "1500")
            @PathVariable @Positive Long id
    ) {
        mkb10CommandService.delete(id);
        return ApiResponse.success(messageResolver.resolve("common.deleted"), null);
    }
}
