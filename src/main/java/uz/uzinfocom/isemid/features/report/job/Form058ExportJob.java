package uz.uzinfocom.isemid.features.report.job;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Bitta report (filterType + filtrlar + tashkilot) uchun generatsiya holati.
 * {@code form058ExportJobCache}da saqlanadi va so'rov thread'i bilan
 * {@link Form058ExportJobWorker}ning fon thread'i o'rtasida bo'lishiladi.
 */
@Getter
@Setter
public class Form058ExportJob {
    private final String key;
    private volatile Form058ExportJobStatus status;
    private volatile Path filePath;
    private volatile String fileName;
    private volatile String errorMessage;
    private final Instant startedAt;
    private volatile Instant completedAt;

    public Form058ExportJob(String key) {
        this.key = key;
        this.status = Form058ExportJobStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }
}
