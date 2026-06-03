package uz.uzinfocom.app.platform.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrganizationSecurityCacheService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.USER_ORGANIZATION_IDS_BY_USER_ID,
            key = "#p0",
            condition = "#p0 != null"
    )
    public Set<Long> loadOrganizationIdsByUserId(Long userId) {
        log.warn("DB HIT: loading organization ids for userId={}", userId);
        return Set.copyOf(userRepository.findOrganizationIdsByUserId(userId));
    }
}