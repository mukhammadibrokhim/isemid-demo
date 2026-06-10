package uz.uzinfocom.app.platform.iam.application.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditUserResponse;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

@Component
@RequiredArgsConstructor
public class AuditResolver {

    private final AuditUserLookupService auditUserLookupService;

    public AuditResponse resolve(AuditableEntity entity) {
        return new AuditResponse(
                entity.getCreatedAt(),
                resolveUser(entity.getCreatedBy()),
                entity.getUpdatedAt(),
                resolveUser(entity.getUpdatedBy())
        );
    }

    private AuditUserResponse resolveUser(Long userId) {
        return userId == null
                ? null
                : auditUserLookupService.findById(userId);
    }
}