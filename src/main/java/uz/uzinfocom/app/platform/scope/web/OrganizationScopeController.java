package uz.uzinfocom.app.platform.scope.web;

import io.swagger.v3.oas.annotations.Operation;
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
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;

@Tag(
        name = "Organization Scope",
        description = "API для получения текущей области доступа пользователя в выбранной организации."
)
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Scope.ROOT)
public class OrganizationScopeController {

    private final OrganizationScopeResolver resolver;
    private final MessageResolver messages;

    @Operation(
            summary = "Получить текущий организационный контекст",
            description = "Возвращает режим доступа, тип организации, уровень организации и территориальные ограничения, рассчитанные для выбранной организации пользователя."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Успешный запрос."
    )
    @GetMapping(ApiPaths.Scope.CURRENT)
    public ApiResponse<CurrentScopeResponse> current() {
        ResolvedOrganizationScope scope = resolver.resolve(
                CurrentOrganizationContext.require()
        );

        return ApiResponse.success(
                messages.resolve("organization.scope.loaded"),
                CurrentScopeResponse.from(scope)
        );
    }
}