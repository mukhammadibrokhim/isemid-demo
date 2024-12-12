package com.abdulatif.emis.repository.user;


import com.abdulatif.emis.model.user.Permission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends CrudRepository<Permission, Long> {

    Permission findByCode(String code);

    Permission findByName(String name);

    @Query(value = "SELECT p FROM Permission p WHERE p.id NOT IN (SELECT p.id FROM Permission p LEFT JOIN p.roles r WHERE r.id = ?1)")
    List<Permission> findNotInRole(Long roleId);

}
