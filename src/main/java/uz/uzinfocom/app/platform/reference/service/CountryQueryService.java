package uz.uzinfocom.app.platform.reference.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.ReferenceCacheNames;
import uz.uzinfocom.app.platform.reference.dto.CountryResponse;
import uz.uzinfocom.app.platform.reference.mapper.CountryMapper;
import uz.uzinfocom.app.platform.reference.repository.CountryRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.List;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class CountryQueryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = ReferenceCacheNames.REF_COUNTRIES, key = "'all'")
    public List<CountryResponse> getAll() {
        return countryRepository.findAllByDeletedFalseOrderBySortOrderAscNameUzAsc()
                .stream()
                .map(countryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CountryResponse getById(Long id) {
        return countryRepository.findByIdAndDeletedFalse(id)
                .map(countryMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.country.not_found_by_id", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = ReferenceCacheNames.REF_COUNTRY_BY_CODE,
            key = "#code.trim().toUpperCase(T(java.util.Locale).ROOT)",
            condition = "#code != null"
    )
    public CountryResponse getByCode(String code) {
        String normalizedCode = ReferenceCodeNormalizer.normalizeCode(code);

        return countryRepository.findByCodeAndDeletedFalse(normalizedCode)
                .map(countryMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("reference.country.not_found_by_code", normalizedCode));
    }
}
