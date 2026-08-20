package uz.uzinfocom.app.modules.iam.application.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.persistence.audit.AuditResolver;
import uz.uzinfocom.app.platform.persistence.audit.AuditResponse;
import uz.uzinfocom.app.platform.persistence.audit.AuditUserResponse;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

@Component
@RequiredArgsConstructor
public class IamAuditResolver implements AuditResolver {

    private final AuditUserLookupService auditUserLookupService;

    @Override
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
