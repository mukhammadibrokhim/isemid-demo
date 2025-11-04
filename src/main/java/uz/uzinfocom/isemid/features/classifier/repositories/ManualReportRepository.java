package uz.uzinfocom.isemid.features.classifier.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.models.enums.ReportType;

import java.util.List;
import java.util.Set;

@Repository
public interface ManualReportRepository extends JpaRepository<ManualReport, Long>, JpaSpecificationExecutor<ManualReport> {
    @Query("SELECT m FROM ManualReport m JOIN m.reportTypes t WHERE t IN :types AND m.includeInTotal = true")
    List<ManualReport> findByReportTypesIn(Set<ReportType> types);
}
