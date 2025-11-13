package uz.uzinfocom.isemid.features.classifier.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.ManualReportRequest;
import uz.uzinfocom.isemid.features.classifier.mappers.ManualReportMapper;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.repositories.ManualReportRepository;
import uz.uzinfocom.isemid.features.classifier.services.ManualReportService;
import uz.uzinfocom.isemid.features.classifier.specifications.ManualReportSpecification;

import java.util.List;

import static org.springframework.http.HttpStatus.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableNotSorting;

@Service
@RequiredArgsConstructor
public class ManualReportServiceImpl implements ManualReportService {

    private final ManualReportRepository repo;
    private final ManualReportMapper mapper;
    private final MessageUtil messageUtil;
    private final CacheManager cacheManager;

    private static final String CACHE_NAME = "manualReportsByType";

    @Override
    public Page<ManualReport> filter(
            int page,
            int size,
            String shortName,
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameKaa,
            String code,
            Boolean includeTotal
    ) {
        Specification<ManualReport> spec =
                ManualReportSpecification.build(shortName, nameUz, nameUzCyril, nameRu, nameKaa, code, includeTotal);

        return repo.findAll(spec, pageableNotSorting(page, size));
    }

    @Override
    public ManualReport create(ManualReportRequest request) {
        ManualReport saved = repo.save(mapper.toEntity(request));

        // Invalidate cache entries related to the types of the newly created report
        evictTypesFromCache(saved);

        return saved;
    }

    @Override
    public ManualReport update(long id, ManualReportRequest request) {
        ManualReport existing = getById(id);

        // Invalidate cache for the old types before updating
        evictTypesFromCache(existing);

        mapper.update(existing, request);
        ManualReport updated = repo.save(existing);

        // Invalidate cache for new types after update
        evictTypesFromCache(updated);

        return updated;
    }

    @Override
    public void delete(long id) {
        ManualReport report = getById(id);

        // Invalidate cache entries related to the types of this report
        evictTypesFromCache(report);

        try {
            repo.delete(report);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(CONFLICT, messageUtil.get("error.cannot_delete_due_to_relations"));
        } catch (Exception ex) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"), ex);
        }
    }

    @Override
    public ManualReport getById(long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    /**
     * Best practice:
     * - Caches entity results for the given report type.
     * - Uses the type string directly as the cache key.
     */
    @Override
    @Cacheable(value = CACHE_NAME, key = "#type")
    public List<ManualReport> getManualReportsForForm(String type) {
        return repo.findByTypeAndIncludeInTotal(type);
    }

    /**
     * Intelligent cache invalidation:
     * - Instead of clearing all cache entries (allEntries = true),
     *   we invalidate only the specific entries related to the affected report types.
     */
    private void evictTypesFromCache(ManualReport report) {
        if (report.getReportTypes() == null || report.getReportTypes().isEmpty()) {
            return;
        }

        var cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) return;

        report.getReportTypes().forEach(cache::evict);
    }
}
