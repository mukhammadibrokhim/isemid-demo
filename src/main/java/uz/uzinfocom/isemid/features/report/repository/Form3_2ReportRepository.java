package uz.uzinfocom.isemid.features.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.Form3_2Report;

public interface Form3_2ReportRepository extends JpaRepository<Form3_2Report, Long>, JpaSpecificationExecutor<Form3_2Report> {
}
