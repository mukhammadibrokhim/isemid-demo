package com.abdulatif.emis.service.user;


import com.abdulatif.emis.model.user.Permission;
import com.abdulatif.emis.repository.user.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public void savePermission(Permission permission) {
        permissionRepository.save(permission);
        log.info("IN save - permission: {} successfully saved", permission);
    }

    public Permission permissionByCode(String code) {
        return permissionRepository.findByCode(code);
    }

    public Permission getPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }

    public List<Permission> findNotInRole(Long roleId) {
        return permissionRepository.findNotInRole(roleId);
    }

}
