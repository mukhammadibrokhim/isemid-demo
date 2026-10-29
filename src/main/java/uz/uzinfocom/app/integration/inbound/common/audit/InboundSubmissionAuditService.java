package uz.uzinfocom.app.integration.inbound.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.inbound.common.web.InboundIntegrationClientResolver;
import uz.uzinfocom.app.integration.inbound.common.web.InboundSubmitterRequest;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboundSubmissionAuditService {

    private final InboundSubmissionAuditRepository repository;
    private final InboundIntegrationClientResolver integrationClientResolver;

    /**
     * Runs after the form itself is already committed, so a failure here is logged
     * rather than propagated - the caller must not be told its (stored) submission failed.
     */
    public void record(
            InboundFormType formType,
            Long formId,
            Long organizationId,
            InboundSubmitterRequest submitter,
            String remoteIp
    ) {
        try {
            repository.save(InboundSubmissionAudit.builder()
                    .createdAt(Instant.now())
                    .formType(formType)
                    .formId(formId)
                    .integrationClientId(integrationClientResolver.resolveSourceIntegrationClientId())
                    .organizationId(organizationId)
                    .submitterPinfl(submitter == null ? null : submitter.pinfl())
                    .submitterFullName(submitter == null ? null : submitter.fullName())
                    .submitterPosition(submitter == null ? null : submitter.position())
                    .submitterPhone(submitter == null ? null : submitter.phone())
                    .remoteIp(remoteIp)
                    .build());
        } catch (RuntimeException failure) {
            log.error("Inbound submission audit failed. formType={}, formId={}, organizationId={}",
                    formType, formId, organizationId, failure);
        }
    }
}
