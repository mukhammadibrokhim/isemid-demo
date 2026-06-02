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
            o.tin,
            o.phone,
            o.active,
            o.stateCode,
            o.cityCode,
            o.levelType,
            o.medicalType
        )
        from Organization o
        where (
              :search = ''
              or lower(coalesce(o.name, '')) like concat('%', :search, '%')
              or lower(coalesce(o.tin, '')) like concat('%', :search, '%')
              or lower(coalesce(o.phone, '')) like concat('%', :search, '%')
              or lower(coalesce(o.stateCode, '')) like concat('%', :search, '%')
              or lower(coalesce(o.cityCode, '')) like concat('%', :search, '%')
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
}
