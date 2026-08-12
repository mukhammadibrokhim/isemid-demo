package uz.uzinfocom.app.platform.reference.application.district.query;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictFilterRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictLookupFilterRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictLookupResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictTableResponse;
import uz.uzinfocom.app.platform.reference.application.district.query.mapper.DistrictMapper;
import uz.uzinfocom.app.platform.reference.application.district.query.projection.DistrictTableProjection;
import uz.uzinfocom.app.platform.reference.application.district.query.specification.DistrictSpecification;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.domain.District;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class DistrictQueryService {

    private static final int DEFAULT_LOOKUP_LIMIT = 20;
    private static final int MAX_LOOKUP_LIMIT = 200;

    private final DistrictRepository districtRepository;
    private final DistrictMapper districtMapper;

    @Transactional(readOnly = true)
    public Page<DistrictTableResponse> findTable(DistrictFilterRequest request) {
        DistrictFilterRequest filter = request == null
                ? new DistrictFilterRequest(null, null, null, null, null, null, null, null, null)
                : request;
        Pageable pageable = PageableUtils.of(filter, DistrictSortFields.ALLOWED_SORT_FIELDS);

        Page<DistrictTableProjection> page = Objects.requireNonNull(districtRepository.findBy(
                DistrictSpecification.byFilter(filter),
                query -> query
                        .as(DistrictTableProjection.class)
                        .page(pageable)), "District Table is returned null"
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
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT) + '-' + " +
                    "T(org.springframework.context.i18n.LocaleContextHolder).getLocale().toLanguageTag()",
            condition = "#code != null"
    )
    public DistrictLookupResponse getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return districtRepository.findByCodeAndDeletedFalse(normalizedCode)
                .map(districtMapper::toLookupResponse)
                .orElseThrow(() -> new NotFoundException("reference.district.not_found_by_code", normalizedCode));
    }

    @Transactional(readOnly = true)
    public List<DistrictLookupResponse> getByParentCode(String parentCode, DistrictLookupFilterRequest request) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        Pageable pageable = PageableUtils.limitOnly(
                request.limit(),
                "nameUz",
                Sort.Direction.ASC,
                DEFAULT_LOOKUP_LIMIT,
                MAX_LOOKUP_LIMIT
        );

        return districtRepository.findAll(
                        DistrictSpecification.byParentCodeAndName(normalizedParentCode, request.name()),
                        pageable
                )
                .map(districtMapper::toLookupResponse)
                .getContent();
    }
}
