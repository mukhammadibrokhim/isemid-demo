package uz.uzinfocom.isemid.features.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.isemid.features.report.model.FormReport7;

import java.sql.Timestamp;
import java.util.Optional;

public interface FormReport7Repository extends JpaRepository<FormReport7, Long>, JpaSpecificationExecutor<FormReport7> {

    // Most recent report for the org before the new period's start — its endPeriodCases
    // becomes the new period's startPeriodCases.
    Optional<FormReport7> findFirstByCreatedOrg_IdAndCreatedAtLessThanOrderByCreatedAtDesc(Long orgId, Timestamp createdAt);
}
