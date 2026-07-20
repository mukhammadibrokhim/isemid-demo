package uz.uzinfocom.app.platform.iam.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.iam.application.shared.dto.MedicalTypeCountProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationGeoProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLevelCountProjection;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationNameProjection;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {

    Optional<Organization> findByUuid(UUID uuid);

    @Query("""
        select o
        from Organization o
        where (:id is null or o.id = :id)
          and (
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
        order by o.name asc, o.id desc
        """)
    List<Organization> lookupOrganizations(
            @Param("search") String search,
            @Param("id") Long id,
            @Param("levelType") OrganizationLevel levelType,
            @Param("medicalType") MedicalType medicalType,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("SELECT o.id FROM Organization o WHERE o.uuid = :uuid and o.active =true")
    Optional<Long> findActiveIdByUuid(@Param("uuid") UUID uuid);

    @Query("""
                SELECT new uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLocalizedName(
                    o.name, o.nameUz, o.nameUzCyril, o.nameRu, o.nameKaa
                )
                FROM Organization o
                WHERE o.id = :id
                  AND o.active = true
            """)
    Optional<OrganizationLocalizedName> findActiveNameFieldsById(@Param("id") Long id);

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
              and (:regionCode is null or o.regionCode = :regionCode)
              and (:districtCode is null or o.districtCode = :districtCode)
            """)
    List<Long> findActiveIdsByRegionAndDistrict(
            @Param("regionCode") String regionCode,
            @Param("districtCode") String districtCode
    );

    long countByActiveTrue();

    /**
     * Grouped organization counts for the home dashboard's medical-institutions
     * breakdown. {@code regionCode}/{@code districtCode} are independently
     * nullable so one query serves ALL (both null), REGION (region only), and
     * DISTRICT (district only) scope modes - ORGANIZATION mode never calls this
     * at all, since a single organization's own medicalType/levelType is
     * already known from {@code ResolvedOrganizationScope} without a DB call.
     */
    @Query("""
            select new uz.uzinfocom.app.platform.iam.application.shared.dto.MedicalTypeCountProjection(o.medicalType, count(o))
            from Organization o
            where o.active = true
              and (:regionCode is null or o.regionCode = :regionCode)
              and (:districtCode is null or o.districtCode = :districtCode)
            group by o.medicalType
            """)
    List<MedicalTypeCountProjection> countActiveByMedicalType(
            @Param("regionCode") String regionCode,
            @Param("districtCode") String districtCode
    );

    /** Same scope rules as {@link #countActiveByMedicalType}, grouped by levelType instead. */
    @Query("""
            select new uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLevelCountProjection(o.levelType, count(o))
            from Organization o
            where o.active = true
              and (:regionCode is null or o.regionCode = :regionCode)
              and (:districtCode is null or o.districtCode = :districtCode)
            group by o.levelType
            """)
    List<OrganizationLevelCountProjection> countActiveByLevelType(
            @Param("regionCode") String regionCode,
            @Param("districtCode") String districtCode
    );

    @Query("""
            select new uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationGeoProjection(
                o.id, o.districtCode
            )
            from Organization o
            where o.active = true
              and o.regionCode = :regionCode
            """)
    List<OrganizationGeoProjection> findActiveIdAndDistrictCodeByRegionCode(@Param("regionCode") String regionCode);

    @Query("""
            select new uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationGeoProjection(
                o.id, o.regionCode
            )
            from Organization o
            where o.active = true
            """)
    List<OrganizationGeoProjection> findActiveIdAndRegionCode();

    /**
     * Every active organization in one district, with its id and raw
     * locale-name fields — for a DISTRICT-scope caller's organization-level
     * geo breakdown (a district-scoped caller has no sub-district geography
     * left, so the unit they see next is their own district's
     * organizations; see {@code Form058DashboardQueryService}).
     */
    @Query("""
            select new uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationNameProjection(
                o.id, o.name, o.nameUz, o.nameUzCyril, o.nameRu, o.nameKaa
            )
            from Organization o
            where o.active = true
              and o.districtCode = :districtCode
            """)
    List<OrganizationNameProjection> findActiveByDistrictCode(@Param("districtCode") String districtCode);

}
