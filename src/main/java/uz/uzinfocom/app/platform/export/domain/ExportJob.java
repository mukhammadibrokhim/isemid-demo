package uz.uzinfocom.app.platform.export.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.platform.persistence.entity.AuditableEntity;

import java.time.Instant;

/**
 * One row per background Excel export request. Visibility for the "my files" list is just
 * {@code createdBy} (inherited from {@link AuditableEntity}, populated by Spring Data
 * auditing) - the user who submitted the request, nothing org-scoped, since an export is a
 * personal download rather than a shared business record.
 * <p>
 * ddl-auto=validate - the DB schema is owned by Liquibase (db.migration/changelog/export/),
 * see {@code Form058} for the same convention.
 */
@Getter
@Setter
@Entity
@Table(
        name = "export_job",
        indexes = {
                @Index(name = "idx_export_job_created_by", columnList = "created_by_id,created_at"),
                @Index(name = "idx_export_job_status_completed", columnList = "status,completed_at")
        }
)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExportJob extends AuditableEntity {

    @Column(name = "export_type", nullable = false, length = 50)
    private String exportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ExportStatus status;

    @Column(name = "filter_snapshot")
    private String filterSnapshot;

    @Column(name = "total_rows")
    private Long totalRows;

    @Column(name = "processed_rows", nullable = false)
    private Long processedRows;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "completed_at")
    private Instant completedAt;

    public void markProcessing(long totalRows) {
        this.status = ExportStatus.PROCESSING;
        this.totalRows = totalRows;
        this.processedRows = 0L;
    }

    public void updateProgress(long processedRows) {
        this.processedRows = processedRows;
    }

    public void markCompleted(String fileName, String filePath, long fileSizeBytes) {
        this.status = ExportStatus.COMPLETED;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.completedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = ExportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }

    public int progressPercent() {
        if (totalRows == null || totalRows == 0) {
            return status == ExportStatus.COMPLETED ? 100 : 0;
        }

        return (int) Math.min(100, Math.round((processedRows * 100.0) / totalRows));
    }

    public boolean isTerminal() {
        return status == ExportStatus.COMPLETED || status == ExportStatus.FAILED;
    }
}
