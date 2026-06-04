package uz.uzinfocom.app.platform.reference.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.ReferenceCacheNames;
import uz.uzinfocom.app.platform.reference.dto.DistrictResponse;
import uz.uzinfocom.app.platform.reference.mapper.DistrictMapper;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.List;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class DistrictQueryService {

    private final DistrictRepository districtRepository;
    private final DistrictMapper districtMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheNames.REF_DISTRICTS, key = "'all'")
    public List<DistrictResponse> getAll() {
        return districtRepository.findAllByDeletedFalseOrderBySortOrderAscNameUzAsc()
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
            cacheNames = ReferenceCacheNames.REF_DISTRICT_BY_CODE,
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
            cacheNames = ReferenceCacheNames.REF_DISTRICTS_BY_PARENT_CODE,
            key = "#parentCode.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#parentCode != null"
    )
    public List<DistrictResponse> getByParentCode(String parentCode) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        return districtRepository
                .findAllByParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(normalizedParentCode)
                .stream()
                .map(districtMapper::toResponse)
                .toList();
    }
}
