package uz.uzinfocom.app.integration.outbound.patientcase.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.inbound.common.web.InboundCallerContext;
import uz.uzinfocom.app.integration.outbound.patientcase.application.PatientCaseLookupService;
import uz.uzinfocom.app.integration.outbound.patientcase.web.dto.PatientCaseResponse;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

/**
 * Outbound counterpart to {@code InboundForm058Controller}/
 * {@code InboundForm0581Controller}: instead of an external system pushing
 * case data to us, a registered integration client pulls a patient's basic
 * info plus their most recently submitted form058/form058-1 back out.
 * Everything about the caller (multi-token-type auth, X-Organization-Id,
 * scope gating, source-key matching) works exactly the same as the inbound
 * side - see {@link InboundCallerContext}, which is shared by both
 * directions.
 */
@Tag(
        name = "Integration - Patient Case",
        description = "Получение сводных сведений о пациенте (данные пациента + последние формы №058/№058-1) "
                + "зарегистрированными интеграционными клиентами."
)
@RestController
@RequestMapping(ApiPaths.Integration.PATIENT_CASE)
@RequiredArgsConstructor
public class PatientCaseController {

    private final PatientCaseLookupService patientCaseLookupService;

    @Operation(
            summary = "Получить сводные сведения о пациенте",
            description = "Находит пациента по значению документа, удостоверяющего личность (PINFL/NNUZB/JSHSHIR, "
                    + "паспорт и т. п.), и возвращает его данные вместе с последней поданной формой №058 и "
                    + "последней поданной формой №058-1 (если они есть). Каждая форма видна, только если "
                    + "организация вызывающего была отправителем или получателем."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PatientCaseResponse lookup(
            @Parameter(description = "Название системы-получателя (см. InboundCallerContext).", required = true)
            @PathVariable String source,
            @Parameter(description = "Значение документа, удостоверяющего личность пациента.", required = true)
            @RequestParam String identifierValue
    ) {
        InboundCallerContext.requireScopeIfIntegrationClient(IntegrationScope.PATIENT_CASE_READ);
        InboundCallerContext.requireMatchingSourceKey(source);

        Long organizationId = InboundCallerContext.resolveSenderOrganizationId();

        return patientCaseLookupService.lookup(identifierValue, organizationId);
    }
}
