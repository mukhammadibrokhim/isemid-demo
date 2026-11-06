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
import uz.uzinfocom.app.platform.audit.application.query.AuditQueryService;
import uz.uzinfocom.app.platform.audit.application.query.dto.AuditEventFilterRequest;
import uz.uzinfocom.app.platform.audit.application.query.dto.AuditEventResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Same audit trail as {@code AuditQueryController}, mirrored here for the dev-monitoring
 * panel — any authenticated {@code DevUser} gets full, unrestricted access under
 * {@code /v1/dev/**} (see {@code DevPanelSecurityConfig}), no further role check needed.
 */
@Tag(
        name = "Dev Panel - Audit",
        description = "Audit trail for Form058/Form0581/Act business events, for the internal developer "
                + "monitoring panel."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevAuditController {

    private final AuditQueryService auditQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @GetMapping(ApiPaths.Dev.AUDIT)
    public PagedResponse<AuditEventResponse> findAll(
            @ParameterObject @Valid @ModelAttribute AuditEventFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<AuditEventResponse> page = auditQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @GetMapping(ApiPaths.Dev.AUDIT_BY_ID)
    public ApiResponse<AuditEventResponse> findById(
            @Parameter(description = "Internal id of the audit event.", required = true)
            @PathVariable @Positive Long id
    ) {
        AuditEventResponse response = auditQueryService.findById(id);
        return ApiResponse.success(messageResolver.resolve("common.success"), response);
    }
}
