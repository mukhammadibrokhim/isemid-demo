package uz.uzinfocom.isemid.features.analytic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticReport;

public interface AnalyticReportRepository extends JpaRepository<AnalyticReport, Long>, JpaSpecificationExecutor<AnalyticReport> {
}
