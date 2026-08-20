package uz.uzinfocom.app.platform.audit.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.audit.application.exception.AuditEventNotFoundException;
import uz.uzinfocom.app.platform.audit.application.query.dto.AuditEventFilterRequest;
import uz.uzinfocom.app.platform.audit.application.query.dto.AuditEventResponse;
import uz.uzinfocom.app.platform.audit.application.query.specification.AuditEventSpecification;
import uz.uzinfocom.app.platform.audit.domain.AuditEvent;
import uz.uzinfocom.app.platform.audit.repository.AuditEventRepository;
import uz.uzinfocom.app.modules.iam.domain.User;
import uz.uzinfocom.app.modules.iam.repository.UserRepository;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> findAll(AuditEventFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, AuditEventSortFields.ALLOWED);

        Page<AuditEvent> page = auditEventRepository
                .findAll(AuditEventSpecification.byFilter(request), pageable);

        Map<Long, User> actorsById = fetchActors(page.getContent());
        return page.map(entity -> toResponse(entity, actorsById.get(entity.getActorUserId())));
    }

    @Transactional(readOnly = true)
    public AuditEventResponse findById(Long id) {
        AuditEvent entity = auditEventRepository.findById(id)
                .orElseThrow(() -> new AuditEventNotFoundException(id));

        User actor = entity.getActorUserId() == null
                ? null
                : userRepository.findById(entity.getActorUserId()).orElse(null);

        return toResponse(entity, actor);
    }

    private Map<Long, User> fetchActors(Iterable<AuditEvent> events) {
        Set<Long> actorIds = StreamSupport.stream(events.spliterator(), false)
                .map(AuditEvent::getActorUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (actorIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private static AuditEventResponse toResponse(AuditEvent entity, User actor) {
        return new AuditEventResponse(
                entity.getId(),
                entity.getEventType(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getActorUserId(),
                actor == null ? null : actor.getUsername(),
                actor == null ? null : actor.getFullName(),
                entity.getReason(),
                resolveChanges(entity),
                entity.getOccurredAt()
        );
    }

    private static Map<String, Object> resolveChanges(AuditEvent entity) {
        return switch (entity.getEventType()) {
            case STATUS_CHANGED -> Map.of("status", Arrays.asList(entity.getOldStatus(), entity.getNewStatus()));
            case ORG_REASSIGNED -> Map.of("orgId", Arrays.asList(entity.getOldOrgId(), entity.getNewOrgId()));
            case CREATED, FIELD_CHANGED -> entity.getChanges();
        };
    }
}
