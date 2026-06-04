package uz.uzinfocom.app.platform.reference.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.ReferenceCacheNames;
import uz.uzinfocom.app.platform.reference.domain.Country;
import uz.uzinfocom.app.platform.reference.dto.CountryCreateRequest;
import uz.uzinfocom.app.platform.reference.dto.CountryResponse;
import uz.uzinfocom.app.platform.reference.dto.CountryUpdateRequest;
import uz.uzinfocom.app.platform.reference.mapper.CountryMapper;
import uz.uzinfocom.app.platform.reference.repository.CountryRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Objects;

@Slf4j
@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class CountryCommandService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_COUNTRIES, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_COUNTRY_BY_CODE, allEntries = true)
    })
    public CountryResponse create(CountryCreateRequest request) {
        String code = ReferenceCodeNormalizer.normalizeCode(request.code());

        if (countryRepository.existsByCode(code)) {
            throw new ConflictException("reference.country.code.already_exists", code);
        }

        Country country = Country.builder()
                .code(code)
                .nameUz(request.nameUz())
                .nameUzCyril(request.nameUzCyril())
                .nameRu(request.nameRu())
                .nameKaa(request.nameKaa())
                .deleted(false)
                .sortOrder(sortOrder(request.sortOrder()))
                .build();

        Country saved = countryRepository.save(country);
        log.debug("Reference country created. id={}, code={}", saved.getId(), saved.getCode());

        return countryMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_COUNTRIES, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_COUNTRY_BY_CODE, allEntries = true)
    })
    public CountryResponse update(Long id, CountryUpdateRequest request) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.country.not_found_by_id", id));

        if (country.isDeleted()) {
            throw new ConflictException("reference.country.update.deleted_conflict", id);
        }

        String code = ReferenceCodeNormalizer.normalizeCode(request.code());

        if (!Objects.equals(country.getCode(), code) && countryRepository.existsByCode(code)) {
            throw new ConflictException("reference.country.code.already_exists", code);
        }

        country.setCode(code);
        country.setNameUz(request.nameUz());
        country.setNameUzCyril(request.nameUzCyril());
        country.setNameRu(request.nameRu());
        country.setNameKaa(request.nameKaa());
        country.setSortOrder(sortOrder(request.sortOrder()));

        Country saved = countryRepository.save(country);
        log.debug("Reference country updated. id={}, code={}", saved.getId(), saved.getCode());

        return countryMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_COUNTRIES, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_COUNTRY_BY_CODE, allEntries = true)
    })
    public void delete(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.country.not_found_by_id", id));

        if (country.isDeleted()) {
            return;
        }

        country.setDeleted(true);
        countryRepository.save(country);
        log.debug("Reference country soft-deleted. id={}, code={}", country.getId(), country.getCode());
    }

    private int sortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }
}
