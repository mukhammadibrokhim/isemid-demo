package uz.uzinfocom.isemid.features.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.FormReport2;

public interface FormReport2Repository extends JpaRepository<FormReport2, Long>, JpaSpecificationExecutor<FormReport2> {
}
