package uz.uzinfocom.isemid.features.report.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.Form3_1Report;

public interface Form3_1ReportRepository extends JpaRepository<Form3_1Report, Long>, JpaSpecificationExecutor<Form3_1Report> {
}
