package uz.uzinfocom.app.platform.iam.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.UserOrganizationRole;

import java.util.List;
import java.util.Optional;

public interface UserOrganizationRoleRepository extends JpaRepository<UserOrganizationRole, Long> {

    @EntityGraph(attributePaths = {
            "organization",
            "role",
            "role.rolePermissions",
            "role.rolePermissions.permission",
            "role.rolePermissions.actions"
    })
    @Query("""
            select distinct uor
            from UserOrganizationRole uor
            join uor.organization o
            join uor.role r
            where uor.user.id = :userId
              and uor.active = true
              and uor.deletedAt is null
            order by o.name asc, r.name asc
            """)
    List<UserOrganizationRole> findByUserIdAndActiveTrueAndDeletedAtIsNullOrderByOrganizationNameAscRoleNameAsc(
            @Param("userId") Long userId
    );

    @EntityGraph(attributePaths = {
            "organization",
            "role",
            "role.rolePermissions",
            "role.rolePermissions.permission",
            "role.rolePermissions.actions"
    })
    @Query("""
            select distinct uor
            from UserOrganizationRole uor
            join uor.organization o
            join uor.role r
            where uor.user.id = :userId
              and uor.organization.id = :organizationId
              and uor.active = true
              and uor.deletedAt is null
            order by r.name asc
            """)
    List<UserOrganizationRole> findByUserIdAndOrganizationIdAndActiveTrueAndDeletedAtIsNullOrderByRoleNameAsc(
            @Param("userId") Long userId,
            @Param("organizationId") Long organizationId
    );

    Optional<UserOrganizationRole> findByUserIdAndOrganizationIdAndRoleIdAndActiveTrueAndDeletedAtIsNull(
            Long userId,
            Long organizationId,
            Long roleId
    );

    Optional<UserOrganizationRole> findTopByUserIdAndOrganizationIdAndRoleIdOrderByIdDesc(
            Long userId,
            Long organizationId,
            Long roleId
    );

    @Query("""
            select distinct r
            from UserOrganizationRole uor
            join uor.role r
            left join fetch r.rolePermissions rp
            left join fetch rp.permission
            left join fetch rp.actions
            where uor.user.id = :userId
              and uor.organization.id = :organizationId
              and uor.active = true
              and uor.deletedAt is null
              and r.active = true
              and r.deleted = false
            """)
    List<Role> findActiveRolesWithPermissions(
            @Param("userId") Long userId,
            @Param("organizationId") Long organizationId
    );

}
