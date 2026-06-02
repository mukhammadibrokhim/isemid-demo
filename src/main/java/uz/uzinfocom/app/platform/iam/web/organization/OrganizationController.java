package uz.uzinfocom.app.platform.iam.web.organization;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.organization.query.OrganizationQueryService;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.*;
import uz.uzinfocom.app.platform.web.api.ApiPaths;
import uz.uzinfocom.app.platform.web.response.ApiResponse;
import uz.uzinfocom.app.platform.web.response.ErrorResponse;
import uz.uzinfocom.app.platform.web.response.PagedResponse;

import java.util.List;

@Tag(
        name = "Организации",
        description = "API для управления медицинскими организациями и организационной структурой."
)
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный запрос.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Пользователь не авторизован.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещён.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Запись не найдена.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@RestController
@RequestMapping(ApiPaths.Organization.BASE)
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationQueryService queryService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Получить список организаций",
            description = "Возвращает постраничный список организаций с фильтрацией по названию, ИНН, региону, району, уровню и типу медицинской организации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping
    public PagedResponse<OrganizationTableResponse> getAll(
            @ParameterObject @Valid @ModelAttribute OrganizationFilerRequest request) {
        Page<OrganizationTableResponse> page = queryService.findTable(request);
        return PagedResponse.fromPage(page, messageResolver.resolve("common.success"));
    }

    @Operation(
            summary = "Получить организацию по идентификатору",
            description = "Возвращает детальную информацию о медицинской организации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.Organization.BY_ID)
    public ApiResponse<OrganizationDetailResponse> get(
            @Parameter(description = "Уникальный идентификатор организации.", required = true)
            @PathVariable Long id
    ) {
        OrganizationDetailResponse response = queryService.findDetail(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(
            summary = "Найти организации для выбора",
            description = "Возвращает краткий список организаций для выпадающих списков и справочного поиска."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.Organization.LOOKUP)
    public ApiResponse<List<OrganizationLookupResponse>> lookup(
            @ParameterObject @Valid @ModelAttribute OrganizationLookupRequest request
    ) {
        List<OrganizationLookupResponse> response = queryService.lookup(request);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @Operation(
            summary = "Найти пользователей организации",
            description = "Возвращает краткий список пользователей выбранной организации для справочного поиска."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.Organization.USERS_LOOKUP_BY_ORGANIZATION_ID)
    public ApiResponse<List<OrganizationUserLookupResponse>> getUserLookupsByOrganization(
            @Parameter(description = "Уникальный идентификатор организации.", required = true)
            @PathVariable(ApiPaths.Organization.ID) Long organizationId,
            @ParameterObject @Valid @ModelAttribute OrganizationUserLookupRequest request
    ) {
        List<OrganizationUserLookupResponse> response =
                queryService.findUserLookupsByOrganizationId(organizationId, request);

        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }
}
