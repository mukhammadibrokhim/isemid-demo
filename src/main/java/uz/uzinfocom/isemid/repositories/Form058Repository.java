package uz.uzinfocom.isemid.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.Form058;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface Form058Repository extends JpaRepository<Form058, Long>, JpaSpecificationExecutor<Form058> {
//    @Query("SELECT f FROM Form058 f " +
//            "JOIN Organization o ON o.uuid = f.createdOrgId " +
//            "JOIN o.state s " +
//            "JOIN Catalog c ON s.code = c.code " +
//            "WHERE c.type = 'state' " +
//            "AND f.createdOrgId = :createdOrgId")
//    List<Form058> findByOrganizationStateAndType(@Param("createdOrgId") UUID createdOrgId);
//
//    @Query("SELECT f FROM Form058 f " +
//            "JOIN Organization o ON o.uuid = f.createdOrgId " +
//            "JOIN o.city s " +
//            "JOIN Catalog c ON s.code = c.code " +
//            "WHERE c.type = 'city' " +
//            "AND f.createdOrgId = :createdOrgId")
//    List<Form058> findByOrganizationCityAndType(@Param("createdOrgId") UUID createdOrgId);

    @Query("""
    SELECT COUNT(f)
    FROM Form058 f
    JOIN Organization o ON f.createdOrgUuid = o.uuid
    WHERE o.city.code = :districtCode
    AND f.createdAt BETWEEN :startDate AND :endDate
""")
    int countByDistrictCodeAndDateRange(
            @Param("districtCode") String districtCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
