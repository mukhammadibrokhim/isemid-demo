package uz.uzinfocom.app.integration.lis.reference.application;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.integration.lis.reference.client.LisReferenceClient;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisCategoryResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisConditionResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisItemTypeResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisOrganizationResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisProfessionResponse;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisReferencePage;
import uz.uzinfocom.app.integration.lis.reference.client.dto.LisResearchTypeResponse;
import uz.uzinfocom.app.integration.lis.reference.config.LisReferenceCacheConfig;

import java.util.List;

/**
 * Cache-aside in front of {@link LisReferenceClient}: read the cache, and on
 * a miss call LIS once and populate it — {@code @Cacheable(sync = true)}
 * gives this for free, including collapsing concurrent misses for the same
 * key onto a single LIS call rather than a thundering herd.
 *
 * <p>Deliberately does NOT try to cache "the whole catalog" for the four
 * paginated lookups (professions/research-types/categories/item-types):
 * LIS's own {@code professions} catalog alone has 12k+ rows, and a single
 * unpaginated fetch against it was observed to take minutes against LIS's
 * test environment — far past this app's {@code integration.lis.read-timeout}.
 * Each distinct {@code (search, page, limit)} combination a caller asks for
 * is cached as its own entry instead (bounded cache size, see
 * {@code ApplicationCacheConfig}).
 */
@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class LisReferenceQueryService {

    private final LisReferenceClient client;

    @Cacheable(cacheNames = LisReferenceCacheConfig.LIS_ORGANIZATIONS, key = "#name == null ? '' : #name", sync = true)
    public List<LisOrganizationResponse> organizations(String name) {
        return client.organizations(name);
    }

    @Cacheable(cacheNames = LisReferenceCacheConfig.LIS_DEPARTMENTS, key = "#organizationId", sync = true)
    public List<LisOrganizationResponse> departments(Long organizationId) {
        return client.departments(organizationId);
    }

    @Cacheable(cacheNames = LisReferenceCacheConfig.LIS_CONDITIONS, key = "'all'", sync = true)
    public List<LisConditionResponse> conditions() {
        return client.conditions();
    }

    @Cacheable(
            cacheNames = LisReferenceCacheConfig.LIS_PROFESSIONS,
            key = "(#search == null ? '' : #search) + ':' + #page + ':' + #limit",
            sync = true
    )
    public LisReferencePage<LisProfessionResponse> professions(String search, int page, int limit) {
        return client.professions(search, page, boundedLimit(limit));
    }

    @Cacheable(
            cacheNames = LisReferenceCacheConfig.LIS_RESEARCH_TYPES,
            key = "(#search == null ? '' : #search) + ':' + #page + ':' + #limit",
            sync = true
    )
    public LisReferencePage<LisResearchTypeResponse> researchTypes(String search, int page, int limit) {
        return client.researchTypes(search, page, boundedLimit(limit));
    }

    @Cacheable(
            cacheNames = LisReferenceCacheConfig.LIS_CATEGORIES,
            key = "(#search == null ? '' : #search) + ':' + #page + ':' + #limit",
            sync = true
    )
    public LisReferencePage<LisCategoryResponse> categories(String search, int page, int limit) {
        return client.categories(search, page, boundedLimit(limit));
    }

    @Cacheable(
            cacheNames = LisReferenceCacheConfig.LIS_ITEM_TYPES,
            key = "(#search == null ? '' : #search) + ':' + #page + ':' + #limit",
            sync = true
    )
    public LisReferencePage<LisItemTypeResponse> itemTypes(String search, int page, int limit) {
        return client.itemTypes(search, page, boundedLimit(limit));
    }

    /**
     * Caps the page size a caller may request — LIS answers a huge
     * {@code limit} (observed: 20000) but takes minutes to do it (see class
     * javadoc), which would starve this app's LIS connection pool/circuit
     * breaker for every other LIS call. 200 comfortably covers a dropdown's
     * worth of results; a caller wanting more should search/paginate instead.
     */
    private int boundedLimit(int requested) {
        int normalized = requested <= 0 ? 50 : requested;
        return Math.min(normalized, 200);
    }
}
