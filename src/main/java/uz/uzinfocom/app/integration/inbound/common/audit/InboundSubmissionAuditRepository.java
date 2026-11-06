package uz.uzinfocom.app.integration.inbound.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundSubmissionAuditRepository extends JpaRepository<InboundSubmissionAudit, Long> {
}
