package uz.uzinfocom.isemid.features.classifier.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;

@Repository
public interface ManualReportRepository extends JpaRepository<ManualReport, Long>, JpaSpecificationExecutor<ManualReport> {
}
