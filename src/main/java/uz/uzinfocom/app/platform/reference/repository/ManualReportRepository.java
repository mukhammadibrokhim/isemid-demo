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
        where :mkb10Code member of m.mkb10Codes and m.deleted = false
    """)
    List<ManualReport> findAllByMkb10CodeAndDeletedFalse(@Param("mkb10Code") String mkb10Code);
}
