package uz.uzinfocom.app.modules.report.form31.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.modules.report.form31.domain.Form31Entry;

public interface Form31EntryRepository
        extends JpaRepository<Form31Entry, Long>, JpaSpecificationExecutor<Form31Entry> {
}
