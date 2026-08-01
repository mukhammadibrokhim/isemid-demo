package uz.uzinfocom.app.platform.audit.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.audit.application.query.AuditQueryService;
import uz.uzinfocom.app.platform.audit.application.query.dto.AuditEventFilterRequest;
import uz.uzinfocom.app.platform.audit.application.query.dto.AuditEventResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Read-only — audit rows are append-only by design (see {@code AuditEventListener}),
 * so there is nothing to "manage" beyond listing/inspecting them. Both
 * {@code isemid_super_admin} and {@code isemid_admin} can read here; the requested
 * SUPER_ADMIN-vs-ADMIN split is already expressed everywhere else in the app (only
 * {@code isemid_super_admin} can act on the underlying Form058/Form0581/Act records
 * this trail describes) — see {@code AdminAccessGuard}.
 */
@Tag(
        name = "Admin - Audit Trail",
        description = "API административного журнала аудита: создание, изменение статуса и передача "
                + "принимающей организации по формам №058, №058-1 и актам."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Audit.ROOT)
@RequiredArgsConstructor
public class AuditQueryController {

    private final AuditQueryService auditQueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @GetMapping
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public PagedResponse<AuditEventResponse> findAll(
            @ParameterObject @Valid @ModelAttribute AuditEventFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<AuditEventResponse> page = auditQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }
}
