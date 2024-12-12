package com.abdulatif.emis.service.user;


import com.abdulatif.emis.model.user.Role;
import com.abdulatif.emis.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<String> findPermissionNames(List<Long> roleIds) {
        return roleRepository.findPermissionNames(roleIds);
    }

    public Role findByCode(String code) {
        Role role = roleRepository.findByCode(code);
        if (role == null) {
            log.warn("IN findByCode - no role found by code: {}", code);
            return null;
        }
        log.info("IN findByCode - role: {} found by code: {}", role, code);
        return role;
    }


    public Role createRole(Role role) {
        Role roleDB = findByCode(role.getCode());
        if (roleDB == null) {
            roleRepository.save(role);
            return role;
        } else {
            log.info("IN create - role with code: {} exists", role.getCode());
            return null;
        }
    }

    public void updateRole(Role role) {
        roleRepository.save(role);
    }



}

