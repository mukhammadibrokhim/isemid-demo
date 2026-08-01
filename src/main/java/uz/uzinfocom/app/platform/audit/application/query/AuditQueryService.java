package uz.uzinfocom.app.platform.audit.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.audit.application.query.dto.AuditEventFilterRequest;
import uz.uzinfocom.app.platform.audit.application.query.dto.AuditEventResponse;
import uz.uzinfocom.app.platform.audit.application.query.specification.AuditEventSpecification;
import uz.uzinfocom.app.platform.audit.domain.AuditEvent;
import uz.uzinfocom.app.platform.audit.repository.AuditEventRepository;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final AuditEventRepository auditEventRepository;

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> findAll(AuditEventFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, AuditEventSortFields.ALLOWED);

        return auditEventRepository
                .findAll(AuditEventSpecification.byFilter(request), pageable)
                .map(AuditQueryService::toResponse);
    }

    private static AuditEventResponse toResponse(AuditEvent entity) {
        return new AuditEventResponse(
                entity.getId(),
                entity.getEventType(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getOldStatus(),
                entity.getNewStatus(),
                entity.getOldOrgId(),
                entity.getNewOrgId(),
                entity.getActorUserId(),
                entity.getReason(),
                entity.getOccurredAt()
        );
    }
}
