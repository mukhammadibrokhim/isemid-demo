package uz.uzinfocom.isemid.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.Organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, JpaSpecificationExecutor<Organization> {
    Optional<Organization> findByUuid(UUID uuid);

    @Query(value = """
    SELECT o.* 
    FROM organization o
    LEFT JOIN catalog s ON o.state = s.code
    LEFT JOIN catalog c ON o.city = c.code
    WHERE (:stateCode IS NULL OR TRIM(:stateCode) = '' OR o.state = :stateCode)
    AND (:cityCode IS NULL OR TRIM(:cityCode) = '' OR o.city = :cityCode)
    AND (:name IS NULL OR TRIM(:name) = '' OR LOWER(o.name) LIKE LOWER(CONCAT('%', :name, '%')))
    ORDER BY o.id ASC
    LIMIT 20
""", nativeQuery = true)
    List<Organization> searchByFilter(
            @Param("stateCode") String stateCode,
            @Param("cityCode") String cityCode,
            @Param("name") String name
    );


    Optional<Organization> findByTin(String tin);
}
