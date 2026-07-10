package uz.uzinfocom.app.platform.iam.web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.UserQueryService;
import uz.uzinfocom.app.platform.iam.web.user.dto.request.UserFilterRequest;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserDetailedResponse;
import uz.uzinfocom.app.platform.iam.web.user.dto.response.UserTableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "API для поиска пользователей и просмотра их организаций.")
@RestController
@RequestMapping(ApiPaths.User.ROOT)
@RequiredArgsConstructor
public class UserController {

    private final UserQueryService userQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить список пользователей",
            description = "Возвращает постраничный список пользователей с возможностью фильтрации."
    )
    @GetMapping
    public PagedResponse<UserTableResponse> findAll(
            @ParameterObject @Valid @ModelAttribute UserFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<UserTableResponse> page = userQueryService.findTable(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить пользователя по идентификатору",
            description = "Возвращает детальную информацию о пользователе."
    )
    @GetMapping(ApiPaths.User.BY_ID)
    public ApiResponse<UserDetailedResponse> findById(
            @Parameter(description = "Идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), userQueryService.getRequiredById(id));
    }

    @Operation(
            summary = "Получить пользователя по UUID",
            description = "Возвращает детальную информацию о пользователе по его UUID."
    )
    @GetMapping(ApiPaths.User.BY_UUID)
    public ApiResponse<UserDetailedResponse> findByUuid(
            @Parameter(description = "UUID пользователя.", required = true)
            @PathVariable UUID uuid
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), userQueryService.getRequiredByUuid(uuid));
    }

    @Operation(
            summary = "Получить организации пользователя",
            description = "Возвращает список организаций, к которым принадлежит указанный пользователь."
    )
    @GetMapping(ApiPaths.User.ORGANIZATIONS)
    public ApiResponse<List<OrganizationShortResponse>> getUserOrganizations(
            @Parameter(description = "Идентификатор пользователя.", required = true)
            @PathVariable(ApiPaths.User.ID) Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), userQueryService.findOrganizations(id));
    }
}
