package uz.uzinfocom.isemid.features.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.Form7Report;

public interface Form7ReportRepository extends JpaRepository<Form7Report, Long>, JpaSpecificationExecutor<Form7Report> {
}
