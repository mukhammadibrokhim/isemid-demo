package uz.uzinfocom.app.platform.reference.application.mkb10.query;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10FilterRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10Response;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10TableResponse;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.mapper.Mkb10Mapper;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.projection.Mkb10TableProjection;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.specification.Mkb10Specification;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.domain.Mkb10;
import uz.uzinfocom.app.platform.reference.repository.Mkb10Repository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class Mkb10QueryService {

    private final Mkb10Repository mkb10Repository;
    private final Mkb10Mapper mkb10Mapper;

    @Transactional(readOnly = true)
    public Page<Mkb10TableResponse> findTable(Mkb10FilterRequest request) {
        Mkb10FilterRequest filter = request == null
                ? new Mkb10FilterRequest(null, null, null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, Mkb10SortFields.ALLOWED_SORT_FIELDS);

        Page<Mkb10TableProjection> page = Objects.requireNonNull(mkb10Repository.findBy(
                Mkb10Specification.byFilter(filter),
                query -> query
                        .as(Mkb10TableProjection.class)
                        .page(pageable)
                ), "MKB-10 Table page is returned null"
        );

        return page.map(mkb10Mapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public Mkb10Response getById(Long id) {
        Mkb10 mkb10 = mkb10Repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("reference.mkb10.not_found_by_id", id));

        return toResponse(mkb10);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_MKB10_BY_CODE,
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#code != null"
    )
    public Mkb10Response getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        Mkb10 mkb10 = mkb10Repository.findByCodeAndDeletedFalse(normalizedCode)
                .orElseThrow(() -> new NotFoundException("reference.mkb10.not_found_by_code", normalizedCode));

        return toResponse(mkb10);
    }

    @Transactional(readOnly = true)
    public List<Mkb10Response> getRoots() {
        return mkb10Repository.findAllByParentIsNullAndDeletedFalseOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_MKB10_CHILDREN_BY_PARENT_ID,
            key = "#parentId",
            condition = "#parentId != null"
    )
    public List<Mkb10Response> getChildren(Long parentId) {
        if (!mkb10Repository.existsById(parentId)) {
            throw new NotFoundException("reference.mkb10.not_found_by_id", parentId);
        }

        return mkb10Repository.findAllByParent_IdAndDeletedFalseOrderByCodeAsc(parentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Mkb10Response toResponse(Mkb10 mkb10) {
        long childrenCount = mkb10Repository.countByParent_IdAndDeletedFalse(mkb10.getId());
        return mkb10Mapper.toResponse(mkb10, childrenCount);
    }
}
