package uz.uzinfocom.app.platform.iam.application.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.shared.cache.AuditCacheConfig;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditUserResponse;
import uz.uzinfocom.app.platform.iam.application.shared.repository.AuditUserQueryRepository;

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