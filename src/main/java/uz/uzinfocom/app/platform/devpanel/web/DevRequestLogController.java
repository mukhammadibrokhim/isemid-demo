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
import uz.uzinfocom.app.platform.devpanel.application.query.DevRequestLogQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevRequestLogDetailResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevRequestLogFilterRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevRequestLogResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Tag(
        name = "Dev Monitoring - Requests",
        description = "Full per-request resource-usage log (every request, not just failures) captured by "
                + "RequestLoggingFilter, for the internal developer monitoring panel. Use the list endpoint "
                + "(filterable by principal/username) to see which resources a user accessed, and the "
                + "by-id endpoint for the full detail of a single request."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevRequestLogController {

    private final DevRequestLogQueryService devRequestLogQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @GetMapping(ApiPaths.Dev.REQUESTS)
    public PagedResponse<DevRequestLogResponse> findAll(
            @ParameterObject @Valid @ModelAttribute DevRequestLogFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<DevRequestLogResponse> page = devRequestLogQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @GetMapping(ApiPaths.Dev.REQUEST_BY_ID)
    public ApiResponse<DevRequestLogDetailResponse> findById(
            @Parameter(description = "Internal id of the request-log entry.", required = true)
            @PathVariable @Positive Long id
    ) {
        DevRequestLogDetailResponse response = devRequestLogQueryService.findById(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }
}
