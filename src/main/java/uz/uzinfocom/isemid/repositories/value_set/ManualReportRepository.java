package uz.uzinfocom.isemid.repositories.value_set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.value_set.ManualReport;

@Repository
public interface ManualReportRepository extends JpaRepository<ManualReport, Long>, JpaSpecificationExecutor<ManualReport> {
}
