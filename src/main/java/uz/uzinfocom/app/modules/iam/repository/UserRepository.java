package uz.uzinfocom.app.modules.iam.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationUserLookupResponse;
import uz.uzinfocom.app.modules.iam.domain.User;

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
            SELECT NEW uz.uzinfocom.app.modules.iam.application.organization.query.dto.response.OrganizationUserLookupResponse(
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

    @Query("""
            SELECT u.id, r.name
            FROM User u
            JOIN u.roles r
            WHERE u.id IN :userIds
              AND r.active = true
              AND r.deleted = false
            ORDER BY r.name
            """)
    java.util.List<Object[]> findRoleNamesByUserIds(@Param("userIds") java.util.Collection<Long> userIds);

    @Query("""
                SELECT u.id
                FROM User u
                JOIN u.organizations o
                WHERE o.id = :organizationId
                  AND u.active = true
            """)
    java.util.List<Long> findActiveIdsByOrganizationId(@Param("organizationId") Long organizationId);

    /**
     * Active users of the active organizations of one medical type in a territory: every such
     * organization of {@code regionCode} when {@code districtCode} is {@code null}, otherwise the
     * ones of that district plus the {@code wideLevels} ones (regional/republican bodies that
     * oversee the district but carry no district code of their own).
     */
    @Query("""
            SELECT DISTINCT u.id
            FROM User u
            JOIN u.organizations o
            WHERE u.active = true
              AND o.active = true
              AND o.medicalType = :medicalType
              AND o.regionCode = :regionCode
              AND (:districtCode IS NULL
                   OR o.districtCode = :districtCode
                   OR o.levelType IN :wideLevels)
            """)
    java.util.List<Long> findActiveIdsByMedicalTypeInTerritory(
            @Param("medicalType") uz.uzinfocom.app.modules.iam.domain.enums.MedicalType medicalType,
            @Param("regionCode") String regionCode,
            @Param("districtCode") String districtCode,
            @Param("wideLevels") java.util.Collection<uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel> wideLevels
    );
}
