package uz.uzinfocom.app.platform.export.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.export.domain.ExportJob;
import uz.uzinfocom.app.platform.export.domain.ExportStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {

    Optional<ExportJob> findByIdAndCreatedBy(Long id, Long createdBy);

    Page<ExportJob> findByCreatedBy(Long createdBy, Pageable pageable);

    Page<ExportJob> findByCreatedByAndExportType(
            Long createdBy,
            @Param("exportType") String exportType,
            Pageable pageable
    );

    List<ExportJob> findByStatusAndCompletedAtBefore(ExportStatus status, Instant completedBefore);
}
