package uz.uzinfocom.app.modules.act.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.act.application.query.ActFilterRequest;
import uz.uzinfocom.app.modules.act.application.query.ActQueryService;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.ActTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Exclusively {@code /v1/acts/*} — listing acts under a specific card lives
 * in {@link CardActQueryController} instead, so this stays a pure "Act" tag
 * in Swagger.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Act.ROOT)
@Tag(
        name = "Act",
        description = "Просмотр актов: личный список актов сотрудника и получение акта по идентификатору."
)
public class ActQueryController {

    private final ActQueryService actQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Мои акты",
            description = "Возвращает постраничный список актов, прикреплённых к текущему авторизованному "
                    + "сотруднику. Область видимости всегда определяется на сервере по авторизованному "
                    + "пользователю — передать чужой идентификатор пользователя через фильтр невозможно."
    )
    @GetMapping(ApiPaths.Act.MINE)
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<ActTableResponse> findMine(
            @ParameterObject @Valid ActFilterRequest filter,
            HttpServletRequest httpRequest
    ) {
        return pagedResponseAssembler
                .toResponse(actQueryService.findMine(filter), messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить акт по идентификатору",
            description = "Возвращает полную детальную информацию по акту."
    )
    @GetMapping(ApiPaths.Act.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ActDetailResponse> byId(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                actQueryService.getById(id)
        );
    }
}
