package uz.uzinfocom.app.platform.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityUserCacheService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.SECURITY_USER_BY_ID,
            key = "#userId",
            condition = "#userId != null",
            unless = "#result == null"
    )
    public Optional<CachedSecurityUser> loadByUserId(Long userId) {
        log.debug("DB HIT: loading security user with organizations for userId={}", userId);
        return userRepository.findSecurityUserWithOrganizationsById(userId)
                .map(this::toCachedUser);
    }

    @CachePut(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.SECURITY_USER_BY_ID,
            key = "#result.userId",
            condition = "#user != null && #user.id != null",
            unless = "#result == null"
    )
    public CachedSecurityUser cacheUser(User user) {
        return toCachedUser(user);
    }

    private CachedSecurityUser toCachedUser(User user) {
        Set<CachedSecurityOrganization> organizations = user.getOrganizations() == null
                ? Set.of()
                : user.getOrganizations().stream()
                .filter(Objects::nonNull)
                .map(this::toCachedOrganization)
                .collect(Collectors.toUnmodifiableSet());

        return new CachedSecurityUser(
                user.getId(),
                user.getUuid(),
                user.getUsername(),
                user.getNnuzb(),
                user.getActive(),
                organizations
        );
    }

    private CachedSecurityOrganization toCachedOrganization(Organization organization) {
        return new CachedSecurityOrganization(
                organization.getId(),
                organization.getUuid(),
                organization.getName(),
                organization.getActive(),
                organization.getLevelType(),
                organization.getMedicalType(),
                organization.getRegionCode(),
                organization.getDistrictCode()
        );
    }
}
