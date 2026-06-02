package uz.uzinfocom.app.platform.iam.web.user;

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
import uz.uzinfocom.app.platform.iam.application.user.query.CurrentUserQueryService;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserMeResponse;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.security.annotation.CurrentUser;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.principal.PrincipalUser;
import uz.uzinfocom.app.platform.web.api.ApiPaths;
import uz.uzinfocom.app.platform.web.response.ApiResponse;
import uz.uzinfocom.app.platform.web.response.ErrorResponse;

import java.util.UUID;

@Tag(
        name = "Текущий пользователь",
        description = "API для получения профиля текущего пользователя, его организаций, ролей и прав доступа."
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
@RequestMapping(ApiPaths.User.BASE)
@RequiredArgsConstructor
public class UserMeController {

    private final MessageResolver messages;
    private final CurrentUserQueryService currentUserQueryService;
    private final OrganizationScopeResolver scopeResolver;

    @Operation(
            summary = "Получить данные текущего пользователя",
            description = "Возвращает профиль текущего пользователя, список организаций, роли и права доступа. Если передан заголовок выбранной организации, дополнительно возвращает роли и права в этой организации."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Успешный запрос."
    )
    @GetMapping(ApiPaths.User.ME)
    public ApiResponse<UserMeResponse> me(@CurrentUser PrincipalUser principal) {
        UUID selectedOrganizationUuid = CurrentOrganizationContext.getOrganizationUuidOptional()
                .orElse(null);

        UserMeResponse response = currentUserQueryService.getCurrentUser(
                principal.id(),
                selectedOrganizationUuid
        );

        return ApiResponse.success(messages.resolve("common.success"), response);
    }
}
