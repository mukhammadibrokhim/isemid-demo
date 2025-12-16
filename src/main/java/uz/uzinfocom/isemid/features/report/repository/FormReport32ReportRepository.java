package uz.uzinfocom.isemid.features.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.FormReport32;

public interface FormReport32ReportRepository extends JpaRepository<FormReport32, Long>, JpaSpecificationExecutor<FormReport32> {
}
