package uz.uzinfocom.app.platform.reference.application.region.query;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionFilterRequest;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionLookupFilterRequest;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionLookupResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.dto.RegionTableResponse;
import uz.uzinfocom.app.platform.reference.application.region.query.mapper.RegionMapper;
import uz.uzinfocom.app.platform.reference.application.region.query.projection.RegionTableProjection;
import uz.uzinfocom.app.platform.reference.application.region.query.specification.RegionSpecification;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.domain.Region;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class RegionQueryService {

    private static final int DEFAULT_LOOKUP_PAGE_SIZE = 20;

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    @Transactional(readOnly = true)
    public Page<RegionTableResponse> findTable(RegionFilterRequest request) {
        RegionFilterRequest filter = request == null
                ? new RegionFilterRequest(null, null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, RegionSortFields.ALLOWED_SORT_FIELDS);

        Page<RegionTableProjection> page = Objects.requireNonNull(regionRepository.findBy(
                RegionSpecification.byFilter(filter),
                query -> query
                        .as(RegionTableProjection.class)
                        .page(pageable)), "Region Table page is returned null"
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
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT) + '-' + " +
                    "T(org.springframework.context.i18n.LocaleContextHolder).getLocale().toLanguageTag()",
            condition = "#code != null"
    )
    public RegionLookupResponse getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return regionRepository.findByCodeAndDeletedFalse(normalizedCode)
                .map(regionMapper::toLookupResponse)
                .orElseThrow(() -> new NotFoundException("reference.region.not_found_by_code", normalizedCode));
    }

    @Transactional(readOnly = true)
    public Page<RegionLookupResponse> getByParentCode(String parentCode, RegionLookupFilterRequest request) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        Pageable pageable = PageableUtils.of(
                request,
                "nameUz",
                Sort.Direction.ASC,
                RegionSortFields.ALLOWED_SORT_FIELDS,
                DEFAULT_LOOKUP_PAGE_SIZE
        );

        Page<Region> page = regionRepository.findAll(
                RegionSpecification.byParentCodeAndName(normalizedParentCode, request.name()),
                pageable
        );

        return page.map(regionMapper::toLookupResponse);
    }
}
