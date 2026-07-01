package uz.uzinfocom.app.platform.iam.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationLookupResponse;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {

    Optional<Organization> findByUuid(UUID uuid);

    @Query("""
             select new uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationLookupResponse(
                 o.id,
                 o.uuid,
                 o.name,
                 o.active,
                 o.levelType,
                 o.medicalType
             )
             from Organization o
             where (
                   :search = ''
                   or lower(coalesce(o.name, '')) like concat('%', :search, '%')
                   or lower(coalesce(o.tin, '')) like concat('%', :search, '%')
                   or lower(coalesce(o.phone, '')) like concat('%', :search, '%')
                   or lower(coalesce(o.regionCode, '')) like concat('%', :search, '%')
                   or lower(coalesce(o.districtCode, '')) like concat('%', :search, '%')
             )
               and (:levelType is null or o.levelType = :levelType)
               and (:medicalType is null or o.medicalType = :medicalType)
               and (:active is null or o.active = :active)
             order by o.name asc, o.id desc\s
            \s""")
    List<OrganizationLookupResponse> lookupOrganizations(
            @Param("search") String search,
            @Param("levelType") OrganizationLevel levelType,
            @Param("medicalType") MedicalType medicalType,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("SELECT o.id FROM Organization o WHERE o.uuid = :uuid and o.active =true")
    Optional<Long> findActiveIdByUuid(@Param("uuid") UUID uuid);

    @Query("""
                SELECT o.name
                FROM Organization o
                WHERE o.id = :id
                  AND o.active = true
            """)
    Optional<String> findActiveNameById(@Param("id") Long id);

    @Query("""
            select o.id
            from Organization o
            where o.active = true
              and o.regionCode = :regionCode
            """)
    List<Long> findActiveIdsByStateCode(@Param("regionCode") String regionCode);

    @Query("""
            select o.id
            from Organization o
            where o.active = true
              and o.districtCode = :districtCode
            """)
    List<Long> findActiveIdsByCityCode(@Param("districtCode") String districtCode);

    @Query("""
            select o.id
            from Organization o
            where o.active = true
              and o.regionCode = :regionCode
              and o.districtCode = :districtCode
            """)
    List<Long> findActiveIdsByStateCodeAndCityCode(
            @Param("regionCode") String regionCode,
            @Param("districtCode") String districtCode
    );
}
