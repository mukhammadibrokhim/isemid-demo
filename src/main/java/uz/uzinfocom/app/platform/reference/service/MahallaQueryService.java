package uz.uzinfocom.app.platform.reference.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.ReferenceCacheNames;
import uz.uzinfocom.app.platform.reference.dto.MahallaResponse;
import uz.uzinfocom.app.platform.reference.mapper.MahallaMapper;
import uz.uzinfocom.app.platform.reference.repository.MahallaRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.List;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class MahallaQueryService {

    private final MahallaRepository mahallaRepository;
    private final MahallaMapper mahallaMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheNames.REF_MAHALLAS, key = "'all'")
    public List<MahallaResponse> getAll() {
        return mahallaRepository.findAllByDeletedFalseOrderBySortOrderAscNameUzAsc()
                .stream()
                .map(mahallaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MahallaResponse getById(Long id) {
        return mahallaRepository.findByIdAndDeletedFalse(id)
                .map(mahallaMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.mahalla.not_found_by_id", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheNames.REF_MAHALLA_BY_CODE,
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#code != null"
    )
    public MahallaResponse getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return mahallaRepository.findByCodeAndDeletedFalse(normalizedCode)
                .map(mahallaMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.mahalla.not_found_by_code", normalizedCode));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheNames.REF_MAHALLAS_BY_PARENT_CODE,
            key = "#parentCode.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#parentCode != null"
    )
    public List<MahallaResponse> getByParentCode(String parentCode) {
        String normalizedParentCode = ReferenceCodeNormalizer.normalizeParentCode(parentCode);

        return mahallaRepository
                .findAllByParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(normalizedParentCode)
                .stream()
                .map(mahallaMapper::toResponse)
                .toList();
    }
}
