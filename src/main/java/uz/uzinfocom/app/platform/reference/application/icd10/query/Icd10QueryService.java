package uz.uzinfocom.app.platform.reference.application.icd10.query;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.icd10.query.dto.Icd10FilterRequest;
import uz.uzinfocom.app.platform.reference.application.icd10.query.dto.Icd10Response;
import uz.uzinfocom.app.platform.reference.application.icd10.query.dto.Icd10TableResponse;
import uz.uzinfocom.app.platform.reference.application.icd10.query.dto.Icd10TreeResponse;
import uz.uzinfocom.app.platform.reference.application.icd10.query.mapper.Icd10Mapper;
import uz.uzinfocom.app.platform.reference.application.icd10.query.projection.Icd10ChildrenCountProjection;
import uz.uzinfocom.app.platform.reference.application.icd10.query.projection.Icd10TableProjection;
import uz.uzinfocom.app.platform.reference.application.icd10.query.specification.Icd10Specification;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.domain.Icd10;
import uz.uzinfocom.app.platform.reference.repository.Icd10Repository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class Icd10QueryService {

    private final Icd10Repository icd10Repository;
    private final Icd10Mapper icd10Mapper;
    private final LocalizedTextResolver localizedTextResolver;

    @Transactional(readOnly = true)
    public Page<Icd10TableResponse> findTable(Icd10FilterRequest request) {
        Icd10FilterRequest filter = request == null
                ? new Icd10FilterRequest(null, null, null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, Icd10SortFields.ALLOWED_SORT_FIELDS);

        Page<Icd10TableProjection> page = Objects.requireNonNull(icd10Repository.findBy(
                Icd10Specification.byFilter(filter),
                query -> query
                        .as(Icd10TableProjection.class)
                        .page(pageable)
                ), "MKB-10 Table page is returned null"
        );

        return page.map(icd10Mapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public Icd10Response getById(Long id) {
        Icd10 icd10 = icd10Repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("reference.icd10.not_found_by_id", id));

        return toResponse(icd10);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_ICD10_BY_CODE,
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#code != null"
    )
    public Icd10Response getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        Icd10 icd10 = icd10Repository.findByCodeAndDeletedFalse(normalizedCode)
                .orElseThrow(() -> new NotFoundException("reference.icd10.not_found_by_code", normalizedCode));

        return toResponse(icd10);
    }

    // Cache key includes the request locale because the response now carries a
    // single, already-resolved `name` (see Icd10TreeResponse) rather than all
    // four language columns - without the locale in the key, whichever locale
    // populated the cache first would leak into every other locale's response.
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_ICD10_ROOTS,
            key = "T(org.springframework.context.i18n.LocaleContextHolder).getLocale().toLanguageTag()"
    )
    public List<Icd10TreeResponse> getRoots() {
        return toTreeResponses(icd10Repository.findAllByParentIsNullAndDeletedFalseOrderByCodeAsc());
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_ICD10_CHILDREN_BY_PARENT_ID,
            key = "#parentId + '-' + T(org.springframework.context.i18n.LocaleContextHolder).getLocale().toLanguageTag()",
            condition = "#parentId != null"
    )
    public List<Icd10TreeResponse> getChildren(Long parentId) {
        if (!icd10Repository.existsById(parentId)) {
            throw new NotFoundException("reference.icd10.not_found_by_id", parentId);
        }

        return toTreeResponses(icd10Repository.findAllByParent_IdAndDeletedFalseOrderByCodeAsc(parentId));
    }

    private Icd10Response toResponse(Icd10 icd10) {
        long childrenCount = icd10Repository.countByParent_IdAndDeletedFalse(icd10.getId());
        return icd10Mapper.toResponse(icd10, childrenCount);
    }

    // Batches the children-count lookup into one GROUP BY query for the whole
    // page instead of one COUNT(*) per node (previously the dominant cost of
    // /roots and /children - dozens/hundreds of round trips per request).
    private List<Icd10TreeResponse> toTreeResponses(List<Icd10> nodes) {
        if (nodes.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> childrenCounts = icd10Repository.countChildrenByParentIds(nodes.stream().map(Icd10::getId).toList())
                .stream()
                .collect(Collectors.toMap(Icd10ChildrenCountProjection::getParentId, Icd10ChildrenCountProjection::getChildrenCount));

        return nodes.stream()
                .map(node -> icd10Mapper.toTreeResponse(
                        node,
                        childrenCounts.getOrDefault(node.getId(), 0L),
                        localizedTextResolver.resolve(node.getNameUz(), node.getNameUzCyril(), node.getNameRu(), node.getNameKaa())
                ))
                .toList();
    }
}
