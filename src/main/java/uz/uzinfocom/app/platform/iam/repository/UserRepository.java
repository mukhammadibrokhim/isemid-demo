package uz.uzinfocom.app.platform.iam.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationUserLookupResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.projection.UserMeProjection;
import uz.uzinfocom.app.platform.iam.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @EntityGraph(attributePaths = {"organizations", "organizationRoles", "organizationRoles.organization", "organizationRoles.role"})
    Optional<User> findByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"organizations", "organizationRoles", "organizationRoles.organization", "organizationRoles.role"})
    Optional<User> findWithOrganizationsById(Long id);

    boolean existsByIdAndOrganizations_Id(Long id, Long organizationId);

    @Query("""
        select new uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationUserLookupResponse(
            u.id,
            u.uuid,
            u.nnuzb,
            u.username,
            u.firstName,
            u.lastName,
            u.middleName,
            u.phoneNumber
        )
        from User u
        join u.organizations o
        where o.id = :organizationId
          and u.active = true
          and (
                :search = ''
                or lower(coalesce(u.firstName, '')) like concat('%', :search, '%')
                or lower(coalesce(u.lastName, '')) like concat('%', :search, '%')
                or lower(coalesce(u.middleName, '')) like concat('%', :search, '%')
                or lower(coalesce(u.username, '')) like concat('%', :search, '%')
                or lower(coalesce(u.nnuzb, '')) like concat('%', :search, '%')
                or lower(coalesce(u.phoneNumber, '')) like concat('%', :search, '%')
          )
        """)
    List<OrganizationUserLookupResponse> findUserLookupsByOrganizationId(
            @Param("organizationId") Long organizationId,
            @Param("search") String search,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "roles",
            "roles.rolePermissions",
            "roles.rolePermissions.permission",
            "roles.rolePermissions.actions"
    })
    @Query("""
        select u
        from User u
        where u.id = :id
    """)
    Optional<UserMeProjection> findMeProjectionById(@Param("id") Long id);
}
