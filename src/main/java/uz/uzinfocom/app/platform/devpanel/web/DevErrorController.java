package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devpanel.application.command.DevErrorCommandService;
import uz.uzinfocom.app.platform.devpanel.application.command.dto.DevErrorStatusUpdateRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.DevErrorQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevErrorDetailResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevErrorFilterRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevErrorResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Tag(
        name = "Dev Panel - Errors",
        description = "Failed-request (status >= 400) history captured by RequestLoggingFilter, "
                + "queryable and resolvable for the internal developer monitoring panel. Use the list "
                + "endpoint for the table/summary view, and the by-id endpoint for the full detail of a "
                + "single failed request."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevErrorController {

    private final DevErrorQueryService devErrorQueryService;
    private final DevErrorCommandService devErrorCommandService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @GetMapping(ApiPaths.Dev.ERRORS)
    public PagedResponse<DevErrorResponse> findAll(
            @ParameterObject @Valid @ModelAttribute DevErrorFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<DevErrorResponse> page = devErrorQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @GetMapping(ApiPaths.Dev.ERROR_BY_ID)
    public ApiResponse<DevErrorDetailResponse> findById(
            @Parameter(description = "Internal id of the failed-request log entry.", required = true)
            @PathVariable @Positive Long id
    ) {
        DevErrorDetailResponse response = devErrorQueryService.findById(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }

    @PatchMapping(ApiPaths.Dev.ERROR_BY_ID)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<Void> updateStatus(
            @Parameter(description = "Internal id of the failed-request log entry.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody DevErrorStatusUpdateRequest request
    ) {
        devErrorCommandService.updateStatus(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"));
    }
}
