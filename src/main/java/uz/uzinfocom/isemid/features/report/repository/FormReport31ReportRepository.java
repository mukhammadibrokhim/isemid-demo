package uz.uzinfocom.isemid.features.report.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.FormReport31;

public interface FormReport31ReportRepository extends JpaRepository<FormReport31, Long>, JpaSpecificationExecutor<FormReport31> {
}
