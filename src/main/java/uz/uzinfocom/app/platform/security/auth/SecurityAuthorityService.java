package uz.uzinfocom.app.platform.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;
import uz.uzinfocom.app.platform.security.authorization.AuthorityNames;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityAuthorityService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
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

        roleRepository.findPermissionAuthorityNamesByRoleIds(roleIds)
                .stream()
                .filter(name -> name != null && !name.isBlank())
                .forEach(authorityNames::add);

        return toGrantedAuthorities(authorityNames);
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