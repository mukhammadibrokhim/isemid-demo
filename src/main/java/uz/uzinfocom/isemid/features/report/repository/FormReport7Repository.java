package uz.uzinfocom.isemid.features.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.FormReport7;

public interface FormReport7Repository extends JpaRepository<FormReport7, Long>, JpaSpecificationExecutor<FormReport7> {
}
