package uz.uzinfocom.app.platform.scope.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.dto.CurrentScopeResponse;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.web.api.ApiPaths;
import uz.uzinfocom.app.platform.web.response.ApiResponse;
import uz.uzinfocom.app.platform.web.response.ErrorResponse;

@Tag(
        name = "Организационный контекст",
        description = "API для получения текущей области доступа пользователя в выбранной организации."
)
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный запрос.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Пользователь не авторизован.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещён.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера.",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Scope.BASE)
public class OrganizationScopeController {

    private final OrganizationScopeResolver resolver;
    private final MessageResolver messages;

    @Operation(
            summary = "Получить текущий организационный контекст",
            description = "Возвращает режим доступа и территориальные ограничения, рассчитанные для выбранной организации пользователя."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Успешный запрос.")
    @GetMapping(ApiPaths.Scope.CURRENT)
    public ApiResponse<CurrentScopeResponse> current() {
        ResolvedOrganizationScope scope = resolver.resolve(CurrentOrganizationContext.require());
        return ApiResponse.success(
                messages.resolve("scope.current"),
                new CurrentScopeResponse(scope.mode(), scope.organizationUuid(), scope.stateCode(), scope.cityCode())
        );
    }
}
