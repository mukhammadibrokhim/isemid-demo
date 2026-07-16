package uz.uzinfocom.app.platform.iam.web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.user.query.CurrentUserQueryService;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserMeResponse;
import uz.uzinfocom.app.platform.security.annotation.CurrentUser;
import uz.uzinfocom.app.platform.security.principal.PrincipalUser;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

@Tag(
        name = "User Me",
        description = "API для получения профиля текущего пользователя, его организаций, ролей и прав доступа."
)
@RestController
@RequestMapping(ApiPaths.User.ROOT)
@RequiredArgsConstructor
public class UserMeController {

    private final MessageResolver messages;
    private final CurrentUserQueryService currentUserQueryService;

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
        UserMeResponse response = currentUserQueryService.getCurrentUser(principal.id());

        return ApiResponse.success(messages.resolve("user.current.loaded"), response);
    }
}
