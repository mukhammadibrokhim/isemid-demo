package uz.uzinfocom.app.platform.reference.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.ReferenceCacheNames;
import uz.uzinfocom.app.platform.reference.domain.Region;
import uz.uzinfocom.app.platform.reference.dto.RegionCreateRequest;
import uz.uzinfocom.app.platform.reference.dto.RegionResponse;
import uz.uzinfocom.app.platform.reference.dto.RegionUpdateRequest;
import uz.uzinfocom.app.platform.reference.mapper.RegionMapper;
import uz.uzinfocom.app.platform.reference.repository.CountryRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Objects;

@Slf4j
@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class RegionCommandService {

    private final RegionRepository regionRepository;
    private final CountryRepository countryRepository;
    private final RegionMapper regionMapper;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_REGIONS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_REGION_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_REGIONS_BY_PARENT_CODE, allEntries = true)
    })
    public RegionResponse create(RegionCreateRequest request) {
        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (regionRepository.existsByCode(code)) {
            throw new ConflictException("reference.region.code.already_exists", code);
        }

        validateCountryParent(parentCode);

        Region region = Region.builder()
                .code(code)
                .parentCode(parentCode)
                .nameUz(request.nameUz())
                .nameUzCyril(request.nameUzCyril())
                .nameRu(request.nameRu())
                .nameKaa(request.nameKaa())
                .deleted(false)
                .sortOrder(sortOrder(request.sortOrder()))
                .build();

        Region saved = regionRepository.save(region);
        log.debug("Reference region created. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return regionMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_REGIONS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_REGION_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_REGIONS_BY_PARENT_CODE, allEntries = true)
    })
    public RegionResponse update(Long id, RegionUpdateRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.region.not_found_by_id", id));

        if (region.isDeleted()) {
            throw new ConflictException("reference.region.update.deleted_conflict", id);
        }

        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (!Objects.equals(region.getCode(), code) && regionRepository.existsByCode(code)) {
            throw new ConflictException("reference.region.code.already_exists", code);
        }

        validateCountryParent(parentCode);

        region.setCode(code);
        region.setParentCode(parentCode);
        region.setNameUz(request.nameUz());
        region.setNameUzCyril(request.nameUzCyril());
        region.setNameRu(request.nameRu());
        region.setNameKaa(request.nameKaa());
        region.setSortOrder(sortOrder(request.sortOrder()));

        Region saved = regionRepository.save(region);
        log.debug("Reference region updated. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return regionMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_REGIONS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_REGION_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_REGIONS_BY_PARENT_CODE, allEntries = true)
    })
    public void delete(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.region.not_found_by_id", id));

        if (region.isDeleted()) {
            return;
        }

        region.setDeleted(true);
        regionRepository.save(region);
        log.debug("Reference region soft-deleted. id={}, code={}, parentCode={}",
                region.getId(), region.getCode(), region.getParentCode());
    }

    private void validateCountryParent(String parentCode) {
        if (!countryRepository.existsByCodeAndDeletedFalse(parentCode)) {
            throw new NotFoundException("reference.country.not_found_by_code", parentCode);
        }
    }

    private int sortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }
}
