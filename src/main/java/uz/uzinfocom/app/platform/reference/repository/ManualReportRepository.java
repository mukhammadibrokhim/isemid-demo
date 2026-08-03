package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.reference.domain.ManualReport;

import java.util.List;
import java.util.Optional;

public interface ManualReportRepository extends JpaRepository<ManualReport, Long>, JpaSpecificationExecutor<ManualReport> {

    Optional<ManualReport> findByIdAndDeletedFalse(Long id);

    Optional<ManualReport> findByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    @Query("""
        select m from ManualReport m
        where :icd10Code member of m.icd10Codes and m.deleted = false
    """)
    List<ManualReport> findAllByIcd10CodeAndDeletedFalse(@Param("icd10Code") String icd10Code);

    @Query("""
        select distinct m from ManualReport m
        join m.reportTypes rt
        where lower(rt) = lower(:reportType) and m.deleted = false
    """)
    List<ManualReport> findAllByReportTypeAndDeletedFalse(@Param("reportType") String reportType);
}
