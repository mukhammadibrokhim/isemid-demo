package uz.uzinfocom.isemid.features.organization.reposiotries;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {

    Optional<Organization> findByUuid(UUID uuid);

    Optional<Organization> findByTin(String tin);

    @Query(value = """
    SELECT *
    FROM organization
    WHERE active = true
      AND (:stateCode IS NULL OR TRIM(:stateCode) = '' OR state_code = :stateCode)
      AND (:cityCode IS NULL OR TRIM(:cityCode) = '' OR city_code = :cityCode)
      AND (:medicalType IS NULL OR TRIM(:medicalType) = '' OR medical_type = :medicalType)
      AND (:name IS NULL OR TRIM(:name) = '' OR LOWER(translate(name, :apostropheChars, '')) LIKE LOWER(CONCAT('%', :name, '%')))
    ORDER BY id DESC
    LIMIT 20
    """, nativeQuery = true)
    List<Organization> searchByFilter(
            @Param("stateCode") String stateCode,
            @Param("cityCode") String cityCode,
            @Param("medicalType") String medicalType,
            @Param("name") String name,
            @Param("apostropheChars") String apostropheChars
    );
}
