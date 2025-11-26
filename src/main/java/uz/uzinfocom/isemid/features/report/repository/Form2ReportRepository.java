package uz.uzinfocom.isemid.features.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.Form2Report;

public interface Form2ReportRepository extends JpaRepository<Form2Report, Long>, JpaSpecificationExecutor<Form2Report> {
}
