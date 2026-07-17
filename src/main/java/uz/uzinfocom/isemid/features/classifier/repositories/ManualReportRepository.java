package uz.uzinfocom.isemid.features.classifier.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;

import java.util.List;

@Repository
public interface ManualReportRepository extends JpaRepository<ManualReport, Long>, JpaSpecificationExecutor<ManualReport> {

    @Query("""
                SELECT DISTINCT mr
                FROM ManualReport mr
                LEFT JOIN FETCH mr.mkb10Codes
                JOIN mr.reportTypes rt
                WHERE rt = :type
                  AND mr.includeInTotal = TRUE
            """)
    List<ManualReport> findByTypeAndIncludeInTotal(@Param("type") String type);

}
