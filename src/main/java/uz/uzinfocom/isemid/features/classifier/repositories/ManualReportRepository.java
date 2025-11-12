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

    @Query(value = """
                SELECT mr.*
                FROM cls_manual_report mr
                JOIN cls_manual_report_types mrt 
                    ON mr.id = mrt.manual_report_id
                WHERE mrt.report_type = :type
                  AND mr.include_in_total = TRUE
            """, nativeQuery = true)
    List<ManualReport> findByTypeAndIncludeInTotal(@Param("type") String type);

}
