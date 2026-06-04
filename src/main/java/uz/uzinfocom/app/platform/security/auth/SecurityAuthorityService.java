package uz.uzinfocom.app.platform.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.authorization.AuthorityNames;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityAuthorityService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CacheManager securityCacheManager;

    @Transactional(readOnly = true)
    @Cacheable(
            cacheManager = "securityCacheManager",
            cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID,
            key = "#userId",
            condition = "#userId != null"
    )
    public Collection<? extends GrantedAuthority> loadAuthoritiesByUserId(Long userId) {
        return userRepository.findForAuthorizationById(userId)
                .map(User::getRoles)
                .map(this::loadAuthoritiesByRoles)
                .orElseGet(Set::of);
    }

    public Collection<? extends GrantedAuthority> loadAuthoritiesByRoles(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        List<Role> availableRoles = roles.stream()
                .filter(Objects::nonNull)
                .filter(Role::isAvailableForAuthorization)
                .filter(role -> role.getId() != null)
                .toList();

        if (availableRoles.isEmpty()) {
            return Set.of();
        }

        LinkedHashSet<String> authorityNames = new LinkedHashSet<>();

        availableRoles.stream()
                .map(Role::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(AuthorityNames::role)
                .forEach(authorityNames::add);

        Set<Long> roleIds = availableRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        loadPermissionAuthorityNamesByRoleIds(roleIds).stream()
                .filter(name -> name != null && !name.isBlank())
                .forEach(authorityNames::add);

        return toGrantedAuthorities(authorityNames);
    }

    private Set<String> loadPermissionAuthorityNamesByRoleIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }

        Cache cache = securityCacheManager.getCache(SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS);

        if (cache == null) {
            return Set.copyOf(roleRepository.findPermissionAuthorityNamesByRoleIds(roleIds));
        }

        String cacheKey = roleIds.stream()
                .filter(Objects::nonNull)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        @SuppressWarnings("unchecked")
        Set<String> cached = cache.get(cacheKey, Set.class);

        if (cached != null) {
            return cached;
        }

        Set<String> authorityNames = Set.copyOf(roleRepository.findPermissionAuthorityNamesByRoleIds(roleIds));
        cache.put(cacheKey, authorityNames);

        return authorityNames;
    }

    private Collection<? extends GrantedAuthority> toGrantedAuthorities(Set<String> authorityNames) {
        if (authorityNames == null || authorityNames.isEmpty()) {
            return Set.of();
        }

        LinkedHashSet<GrantedAuthority> authorities = new LinkedHashSet<>();

        authorityNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);

        return Set.copyOf(authorities);
    }
}
