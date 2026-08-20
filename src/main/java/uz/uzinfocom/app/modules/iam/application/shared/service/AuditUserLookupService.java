package uz.uzinfocom.app.modules.iam.application.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.iam.application.shared.cache.AuditCacheConfig;
import uz.uzinfocom.app.modules.iam.application.shared.repository.AuditUserQueryRepository;
import uz.uzinfocom.app.platform.persistence.audit.AuditUserResponse;

@Service
@RequiredArgsConstructor
public class AuditUserLookupService {

    private final AuditUserQueryRepository auditUserQueryRepository;

    @Cacheable(
            cacheNames = AuditCacheConfig.AUDIT_USER_BY_ID,
            key = "#userId",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public AuditUserResponse findById(Long userId) {
        return auditUserQueryRepository.findAuditUserById(userId)
                .orElse(null);
    }
}