package com.abdulatif.emis.repository.user;

import com.abdulatif.emis.model.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends PagingAndSortingRepository<Role, Long>, JpaRepository<Role, Long>, QueryByExampleExecutor<Role> {

    @Query("SELECT p.name FROM Role r LEFT JOIN r.permissions p WHERE r.status = 'ACTIVE' and r.id IN(?1) ORDER BY p.name")
    List<String> findPermissionNames(List<Long> roleIds);

    Role findByCode(String code);

    Role findByName(String name);

    @Query("select d from Role d where d.status = 'ACTIVE' and d.id = ?1")
    Optional<Role> findActiveById(Long id);

}

