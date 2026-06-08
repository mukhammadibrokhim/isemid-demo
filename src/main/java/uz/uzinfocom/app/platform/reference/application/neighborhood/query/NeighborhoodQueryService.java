package uz.uzinfocom.app.platform.reference.application.neighborhood.query;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodFilterRequest;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodTableResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.mapper.NeighborhoodMapper;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.projection.NeighborhoodTableProjection;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.specification.NeighborhoodSpecification;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.repository.NeighborhoodRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class NeighborhoodQueryService {

    private final NeighborhoodRepository neighborhoodRepository;
    private final NeighborhoodMapper neighborhoodMapper;

    @Transactional(readOnly = true)
    public Page<NeighborhoodTableResponse> findTable(NeighborhoodFilterRequest request) {
        NeighborhoodFilterRequest filter = request == null
                ? new NeighborhoodFilterRequest(null, null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, NeighborhoodSortFields.ALLOWED_SORT_FIELDS);

        Page<NeighborhoodTableProjection> page = Objects.requireNonNull(neighborhoodRepository.findBy(
                NeighborhoodSpecification.byFilter(filter),
                query -> query
                        .as(NeighborhoodTableProjection.class)
                        .page(pageable)), "Neighbourhood table page is returned null"
        );

        return page.map(neighborhoodMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOODS, key = "'all'")
    public List<NeighborhoodResponse> getAll() {
        return neighborhoodRepository.findAllByDeletedFalseOrderByNameUzAsc()
                .stream()
                .map(neighborhoodMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NeighborhoodResponse getById(Long id) {
        return neighborhoodRepository.findByIdAndDeletedFalse(id)
                .map(neighborhoodMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.neighborhood.not_found_by_id", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOOD_BY_CODE,
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#code != null"
    )
    public NeighborhoodResponse getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return neighborhoodRepository.findByCodeAndDeletedFalse(normalizedCode)
                .map(neighborhoodMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.neighborhood.not_found_by_code", normalizedCode));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOODS_BY_PARENT_CODE,
            key = "#parentCode.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#parentCode != null"
    )
    public List<NeighborhoodResponse> getByParentCode(String parentCode) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        return neighborhoodRepository
                .findAllByParentCodeAndDeletedFalseOrderByNameUzAsc(normalizedParentCode)
                .stream()
                .map(neighborhoodMapper::toResponse)
                .toList();
    }
}
