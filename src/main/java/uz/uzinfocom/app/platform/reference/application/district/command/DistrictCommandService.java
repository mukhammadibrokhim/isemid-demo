package uz.uzinfocom.app.platform.reference.application.district.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.domain.District;
import uz.uzinfocom.app.platform.reference.application.district.dto.DistrictCreateRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.dto.DistrictResponse;
import uz.uzinfocom.app.platform.reference.application.district.dto.DistrictUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.district.query.mapper.DistrictMapper;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Objects;

@Slf4j
@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class DistrictCommandService {

    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;
    private final DistrictMapper districtMapper;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_DISTRICTS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_DISTRICT_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_DISTRICTS_BY_PARENT_CODE, allEntries = true)
    })
    public DistrictResponse create(DistrictCreateRequest request) {
        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (districtRepository.existsByCode(code)) {
            throw new ConflictException("reference.district.code.already_exists", code);
        }

        validateRegionParent(parentCode);

        District district = District.builder()
                .code(code)
                .parentCode(parentCode)
                .nameUz(request.nameUz())
                .nameUzCyril(request.nameUzCyril())
                .nameRu(request.nameRu())
                .nameKaa(request.nameKaa())
                .deleted(false)
                .sortOrder(sortOrder(request.sortOrder()))
                .build();

        District saved = districtRepository.save(district);
        log.debug("Reference district created. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return districtMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_DISTRICTS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_DISTRICT_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_DISTRICTS_BY_PARENT_CODE, allEntries = true)
    })
    public DistrictResponse update(Long id, DistrictUpdateRequest request) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.district.not_found_by_id", id));

        if (district.isDeleted()) {
            throw new ConflictException("reference.district.update.deleted_conflict", id);
        }

        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (!Objects.equals(district.getCode(), code) && districtRepository.existsByCode(code)) {
            throw new ConflictException("reference.district.code.already_exists", code);
        }

        validateRegionParent(parentCode);

        district.setCode(code);
        district.setParentCode(parentCode);
        district.setNameUz(request.nameUz());
        district.setNameUzCyril(request.nameUzCyril());
        district.setNameRu(request.nameRu());
        district.setNameKaa(request.nameKaa());
        district.setSortOrder(sortOrder(request.sortOrder()));

        District saved = districtRepository.save(district);
        log.debug("Reference district updated. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return districtMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_DISTRICTS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_DISTRICT_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_DISTRICTS_BY_PARENT_CODE, allEntries = true)
    })
    public void delete(Long id) {
        District district = districtRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.district.not_found_by_id", id));

        if (district.isDeleted()) {
            return;
        }

        district.setDeleted(true);
        districtRepository.save(district);
        log.debug("Reference district soft-deleted. id={}, code={}, parentCode={}",
                district.getId(), district.getCode(), district.getParentCode());
    }

    private void validateRegionParent(String parentCode) {
        if (!regionRepository.existsByCodeAndDeletedFalse(parentCode)) {
            throw new NotFoundException("reference.region.not_found_by_code", parentCode);
        }
    }

    private int sortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }
}
