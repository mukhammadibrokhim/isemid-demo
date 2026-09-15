package uz.uzinfocom.app.modules.report.form2.manual.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.modules.report.form2.manual.domain.Form2ManualEntry;

public interface Form2ManualEntryRepository
        extends JpaRepository<Form2ManualEntry, Long>, JpaSpecificationExecutor<Form2ManualEntry> {
}
