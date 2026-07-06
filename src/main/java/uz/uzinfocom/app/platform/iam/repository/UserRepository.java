package uz.uzinfocom.app.platform.iam.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationUserLookupResponse;
import uz.uzinfocom.app.platform.iam.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @EntityGraph(attributePaths = {"organizations", "roles"})
    Optional<User> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"organizations", "roles"})
    Optional<User> findWithOrganizationsById(Long id);

    @EntityGraph(attributePaths = {"organizations"})
    @Query("""
                SELECT DISTINCT u
                FROM User u
                WHERE u.id = :id
    """)
    Optional<User> findSecurityUserWithOrganizationsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {
            "roles",
            "roles.rolePermissions",
            "roles.rolePermissions.permission",
            "roles.rolePermissions.actions"
    })
    @Query("""
                SELECT DISTINCT u
                FROM User u
                WHERE u.id = :id
            """)
    Optional<User> findForAuthorizationById(@Param("id") Long id);

    @Query("""
            SELECT NEW uz.uzinfocom.app.platform.iam.application.organization.query.dto.response.OrganizationUserLookupResponse(
                u.id,
                u.uuid,
                u.firstName,
                u.lastName,
                u.middleName,
                u.phoneNumber
            )
            FROM User u
            JOIN u.organizations o
            WHERE o.id = :organizationId
              AND u.active = true
              AND (
                    :search = ''
                    OR lower(coalesce(u.firstName, '')) LIKE concat('%', :search, '%')
                    OR lower(coalesce(u.lastName, '')) LIKE concat('%', :search, '%')
                    OR lower(coalesce(u.middleName, '')) LIKE concat('%', :search, '%')
                    OR lower(coalesce(u.username, '')) LIKE concat('%', :search, '%')
                    OR lower(coalesce(u.nnuzb, '')) LIKE concat('%', :search, '%')
                    OR lower(coalesce(u.phoneNumber, '')) LIKE concat('%', :search, '%')
              )
            """)
    org.springframework.data.domain.Page<OrganizationUserLookupResponse> findUserLookupsByOrganizationId(
            @Param("organizationId") Long organizationId,
            @Param("search") String search,
            Pageable pageable
    );
}
