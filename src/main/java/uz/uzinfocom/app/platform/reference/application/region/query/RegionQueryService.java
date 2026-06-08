package uz.uzinfocom.app.platform.reference.application.region.query;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionFilterRequest;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionTableResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.mapper.RegionMapper;
import uz.uzinfocom.app.platform.reference.application.region.query.projection.RegionTableProjection;
import uz.uzinfocom.app.platform.reference.application.region.query.specification.RegionSpecification;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class RegionQueryService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    @Transactional(readOnly = true)
    public Page<RegionTableResponse> findTable(RegionFilterRequest request) {
        RegionFilterRequest filter = request == null
                ? new RegionFilterRequest(null, null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, RegionSortFields.ALLOWED_SORT_FIELDS);

        Page<RegionTableProjection> page = regionRepository.findBy(
                RegionSpecification.byFilter(filter),
                query -> query
                        .as(RegionTableProjection.class)
                        .page(pageable)
        );

        return page.map(regionMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_REGIONS, key = "'all'")
    public List<RegionResponse> getAll() {
        return regionRepository.findAllByDeletedFalseOrderByNameUzAsc()
                .stream()
                .map(regionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegionResponse getById(Long id) {
        return regionRepository.findByIdAndDeletedFalse(id)
                .map(regionMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.region.not_found_by_id", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_REGION_BY_CODE,
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#code != null"
    )
    public RegionResponse getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return regionRepository.findByCodeAndDeletedFalse(normalizedCode)
                .map(regionMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.region.not_found_by_code", normalizedCode));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_REGIONS_BY_PARENT_CODE,
            key = "#parentCode.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#parentCode != null"
    )
    public List<RegionResponse> getByParentCode(String parentCode) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        return regionRepository
                .findAllByParentCodeAndDeletedFalseOrderByNameUzAsc(normalizedParentCode)
                .stream()
                .map(regionMapper::toResponse)
                .toList();
    }
}
