package uz.uzinfocom.app.modules.report.form7.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.modules.report.form7.domain.Form7Entry;

public interface Form7EntryRepository
        extends JpaRepository<Form7Entry, Long>, JpaSpecificationExecutor<Form7Entry> {
}
