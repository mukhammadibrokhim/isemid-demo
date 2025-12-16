package uz.uzinfocom.isemid.features.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.FormReport12;

public interface FormReport12Repository extends JpaRepository<FormReport12, Long>, JpaSpecificationExecutor<FormReport12> {
}
