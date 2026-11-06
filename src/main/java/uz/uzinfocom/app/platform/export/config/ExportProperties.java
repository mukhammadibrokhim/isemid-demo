package uz.uzinfocom.app.platform.export.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.export")
public class ExportProperties {

    @NotBlank
    private String storageDir = "exports";

    @Min(1)
    private long maxRows = 200_000;

    @Min(1)
    private int retentionDays = 7;

    @NotBlank
    private String cleanupCron = "0 0 3 * * *";

    /** Exports running at once, per instance - see {@code ExportExecutorConfig}. */
    @Min(1)
    private int maxConcurrentJobs = 3;

    /** Exports waiting for a free worker before new submits are rejected. */
    @Min(1)
    private int maxQueuedJobs = 50;

    /** PENDING + PROCESSING jobs one user may have at a time. */
    @Min(1)
    private int maxActiveJobsPerUser = 3;
}
