package com.abdulatif.emis.security.jwt;

import com.abdulatif.emis.model.user.Permission;
import com.abdulatif.emis.model.user.Role;
import com.abdulatif.emis.model.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.abdulatif.emis.model.enums.Status.ACTIVE;


/**
 * Implementation of Factory Method for class {@link JwtUser}.
 */

public final class JwtUserFactory {

    public static JwtUser create(User user) {
        return new JwtUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                mapToGrantedAuthorities(user.getRoles().stream().toList()),
                user.getStatus().equals(ACTIVE)
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<Role> userRole) {
        Set<Permission> permissions = new HashSet<>(userRole.size());
        List<String> codes = new ArrayList<>(userRole.size());
        userRole.forEach(role -> permissions.addAll(role.getPermissions()));
        List<GrantedAuthority> collect = permissions.stream().map(permission -> new SimpleGrantedAuthority(permission.getName())).collect(Collectors.toList());
        userRole.forEach(role -> codes.add(role.getCode()));
        collect.add(new SimpleGrantedAuthority(codes.toString()));
        return collect;
    }
}
