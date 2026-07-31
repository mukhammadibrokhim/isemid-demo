package uz.uzinfocom.app.modules.report.form32.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.modules.report.form32.domain.Form32Entry;

public interface Form32EntryRepository
        extends JpaRepository<Form32Entry, Long>, JpaSpecificationExecutor<Form32Entry> {
}
