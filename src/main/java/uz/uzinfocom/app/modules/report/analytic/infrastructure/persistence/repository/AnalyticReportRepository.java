package uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReport;

public interface AnalyticReportRepository
        extends JpaRepository<AnalyticReport, Long>, JpaSpecificationExecutor<AnalyticReport> {
}
