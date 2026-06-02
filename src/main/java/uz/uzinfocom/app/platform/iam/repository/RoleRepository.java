package uz.uzinfocom.app.platform.iam.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.iam.domain.Role;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    @Query("""
        select r.id
        from Role r
        where lower(trim(r.name)) = :normalizedName
          and r.deletedAt is null
    """)
    Optional<Long> findIdByNormalizedName(@Param("normalizedName") String normalizedName);

    @Query("""
        select r
        from Role r
        where lower(trim(r.name)) = :normalizedName
          and r.deletedAt is null
    """)
    Optional<Role> findByNormalizedName(@Param("normalizedName") String normalizedName);

    @EntityGraph(attributePaths = {
            "rolePermissions",
            "rolePermissions.permission",
            "rolePermissions.actions"
    })
    @Query("""
        select distinct r
        from Role r
        where r.id = :id
          and r.deletedAt is null
    """)
    Optional<Role> findWithPermissionsById(@Param("id") Long id);

    @Query("""
                select count(r) > 0
                from Role r
                where lower(trim(r.name)) = lower(trim(:name))
                  and r.deletedAt is null
            """)
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("""
        select count(r) > 0
        from Role r
        where lower(trim(r.name)) = lower(trim(:name))
          and r.id <> :id
          and r.deletedAt is null
    """)
    boolean existsByNameIgnoreCaseAndIdNot(
            @Param("name") String name,
            @Param("id") Long id
    );

    @Query(value = """
        select distinct concat(
            'PERMISSION_',
            upper(p.subject),
            '_',
            upper(cast(rpa.action as text))
        )
        from role_permissions rp
        join permission p on p.id = rp.permission_id
        join role_permission_actions rpa on rpa.role_permission_id = rp.id
        where rp.role_id in (:roleIds)
          and p.active = true
          and p.deleted_at is null
        """, nativeQuery = true)
    Set<String> findPermissionAuthorityNamesByRoleIds(@Param("roleIds") Collection<Long> roleIds);
}