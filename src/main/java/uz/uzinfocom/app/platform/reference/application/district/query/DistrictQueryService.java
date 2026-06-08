package uz.uzinfocom.app.platform.reference.application.district.query;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictFilterRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictTableResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.mapper.DistrictMapper;
import uz.uzinfocom.app.platform.reference.application.district.query.projection.DistrictTableProjection;
import uz.uzinfocom.app.platform.reference.application.district.query.specification.DistrictSpecification;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class DistrictQueryService {

    private final DistrictRepository districtRepository;
    private final DistrictMapper districtMapper;

    @Transactional(readOnly = true)
    public Page<DistrictTableResponse> findTable(DistrictFilterRequest request) {
        DistrictFilterRequest filter = request == null
                ? new DistrictFilterRequest(null, null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, DistrictSortFields.ALLOWED_SORT_FIELDS);

        Page<DistrictTableProjection> page = districtRepository.findBy(
                DistrictSpecification.byFilter(filter),
                query -> query
                        .as(DistrictTableProjection.class)
                        .page(pageable)
        );

        return page.map(districtMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheConfig.REF_DISTRICTS, key = "'all'")
    public List<DistrictResponse> getAll() {
        return districtRepository.findAllByDeletedFalseOrderByNameUzAsc()
                .stream()
                .map(districtMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DistrictResponse getById(Long id) {
        return districtRepository.findByIdAndDeletedFalse(id)
                .map(districtMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.district.not_found_by_id", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_DISTRICT_BY_CODE,
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#code != null"
    )
    public DistrictResponse getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return districtRepository.findByCodeAndDeletedFalse(normalizedCode)
                .map(districtMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.district.not_found_by_code", normalizedCode));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheConfig.REF_DISTRICTS_BY_PARENT_CODE,
            key = "#parentCode.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#parentCode != null"
    )
    public List<DistrictResponse> getByParentCode(String parentCode) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        return districtRepository
                .findAllByParentCodeAndDeletedFalseOrderByNameUzAsc(normalizedParentCode)
                .stream()
                .map(districtMapper::toResponse)
                .toList();
    }
}
