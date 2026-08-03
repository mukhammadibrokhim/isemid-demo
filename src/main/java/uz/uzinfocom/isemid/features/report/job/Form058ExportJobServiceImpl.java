package uz.uzinfocom.isemid.features.report.job;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.nio.file.Paths;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Form058ExportJobServiceImpl implements Form058ExportJobService {
    private final Cache<String, Form058ExportJob> form058ExportJobCache;
    private final Form058ExportJobWorker jobWorker;

    @Value("${isemid.report.export-dir:./export-files}")
    private String exportDir;

    @Override
    public Form058ExportJob getOrStart(FilterType filterType, LocalDate fromDate, LocalDate toDate,
                                       String stateCode, String cityCode, User user) {
        String key = Form058ExportJobKeyBuilder.build(filterType, fromDate, toDate, stateCode, cityCode, user);

        Form058ExportJob existing = form058ExportJobCache.getIfPresent(key);
        if (existing != null && existing.getStatus() == Form058ExportJobStatus.FAILED) {
            form058ExportJobCache.invalidate(key);
        }

        return form058ExportJobCache.get(key, k -> {
            Form058ExportJob job = new Form058ExportJob(k);
            jobWorker.generate(job, Paths.get(exportDir), filterType, fromDate, toDate, stateCode, cityCode, user);
            return job;
        });
    }
}
