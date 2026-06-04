package uz.uzinfocom.app.platform.reference.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.ReferenceCacheNames;
import uz.uzinfocom.app.platform.reference.dto.RegionResponse;
import uz.uzinfocom.app.platform.reference.mapper.RegionMapper;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.List;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class RegionQueryService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheNames.REF_REGIONS, key = "'all'")
    public List<RegionResponse> getAll() {
        return regionRepository.findAllByDeletedFalseOrderBySortOrderAscNameUzAsc()
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
            cacheNames = ReferenceCacheNames.REF_REGION_BY_CODE,
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
            cacheNames = ReferenceCacheNames.REF_REGIONS_BY_PARENT_CODE,
            key = "#parentCode.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#parentCode != null"
    )
    public List<RegionResponse> getByParentCode(String parentCode) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        return regionRepository
                .findAllByParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(normalizedParentCode)
                .stream()
                .map(regionMapper::toResponse)
                .toList();
    }
}
