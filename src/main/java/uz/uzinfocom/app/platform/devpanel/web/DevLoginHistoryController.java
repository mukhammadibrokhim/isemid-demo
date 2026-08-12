package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devpanel.application.query.DevLoginHistoryQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevLoginHistoryDetailResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevLoginHistoryFilterRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevLoginHistoryResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Tag(
        name = "Dev Monitoring - Logins",
        description = "Login-attempt (success and failure) history for /v1/auth/login/{provider}, "
                + "for the internal developer monitoring panel. Use the list endpoint for the "
                + "table/summary view, and the by-id endpoint for the full detail of a single attempt."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevLoginHistoryController {

    private final DevLoginHistoryQueryService devLoginHistoryQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @GetMapping(ApiPaths.Dev.LOGINS)
    public PagedResponse<DevLoginHistoryResponse> findAll(
            @ParameterObject @Valid @ModelAttribute DevLoginHistoryFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<DevLoginHistoryResponse> page = devLoginHistoryQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @GetMapping(ApiPaths.Dev.LOGIN_BY_ID)
    public ApiResponse<DevLoginHistoryDetailResponse> findById(
            @Parameter(description = "Internal id of the login-history entry.", required = true)
            @PathVariable @Positive Long id
    ) {
        DevLoginHistoryDetailResponse response = devLoginHistoryQueryService.findById(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }
}
