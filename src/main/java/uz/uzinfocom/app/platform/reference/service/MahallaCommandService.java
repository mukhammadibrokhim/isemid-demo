package uz.uzinfocom.app.platform.reference.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.ReferenceCacheNames;
import uz.uzinfocom.app.platform.reference.domain.Mahalla;
import uz.uzinfocom.app.platform.reference.dto.MahallaCreateRequest;
import uz.uzinfocom.app.platform.reference.dto.MahallaResponse;
import uz.uzinfocom.app.platform.reference.dto.MahallaUpdateRequest;
import uz.uzinfocom.app.platform.reference.mapper.MahallaMapper;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.MahallaRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Objects;

@Slf4j
@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class MahallaCommandService {

    private final MahallaRepository mahallaRepository;
    private final DistrictRepository districtRepository;
    private final MahallaMapper mahallaMapper;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_MAHALLAS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_MAHALLA_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_MAHALLAS_BY_PARENT_CODE, allEntries = true)
    })
    public MahallaResponse create(MahallaCreateRequest request) {
        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (mahallaRepository.existsByCode(code)) {
            throw new ConflictException("reference.mahalla.code.already_exists", code);
        }

        validateDistrictParent(parentCode);

        Mahalla mahalla = Mahalla.builder()
                .code(code)
                .parentCode(parentCode)
                .nameUz(request.nameUz())
                .nameUzCyril(request.nameUzCyril())
                .nameRu(request.nameRu())
                .nameKaa(request.nameKaa())
                .deleted(false)
                .sortOrder(sortOrder(request.sortOrder()))
                .build();

        Mahalla saved = mahallaRepository.save(mahalla);
        log.debug("Reference mahalla created. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return mahallaMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_MAHALLAS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_MAHALLA_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_MAHALLAS_BY_PARENT_CODE, allEntries = true)
    })
    public MahallaResponse update(Long id, MahallaUpdateRequest request) {
        Mahalla mahalla = mahallaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.mahalla.not_found_by_id", id));

        if (mahalla.isDeleted()) {
            throw new ConflictException("reference.mahalla.update.deleted_conflict", id);
        }

        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (!Objects.equals(mahalla.getCode(), code) && mahallaRepository.existsByCode(code)) {
            throw new ConflictException("reference.mahalla.code.already_exists", code);
        }

        validateDistrictParent(parentCode);

        mahalla.setCode(code);
        mahalla.setParentCode(parentCode);
        mahalla.setNameUz(request.nameUz());
        mahalla.setNameUzCyril(request.nameUzCyril());
        mahalla.setNameRu(request.nameRu());
        mahalla.setNameKaa(request.nameKaa());
        mahalla.setSortOrder(sortOrder(request.sortOrder()));

        Mahalla saved = mahallaRepository.save(mahalla);
        log.debug("Reference mahalla updated. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return mahallaMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_MAHALLAS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_MAHALLA_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheNames.REF_MAHALLAS_BY_PARENT_CODE, allEntries = true)
    })
    public void delete(Long id) {
        Mahalla mahalla = mahallaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.mahalla.not_found_by_id", id));

        if (mahalla.isDeleted()) {
            return;
        }

        mahalla.setDeleted(true);
        mahallaRepository.save(mahalla);
        log.debug("Reference mahalla soft-deleted. id={}, code={}, parentCode={}",
                mahalla.getId(), mahalla.getCode(), mahalla.getParentCode());
    }

    private void validateDistrictParent(String parentCode) {
        if (!districtRepository.existsByCodeAndDeletedFalse(parentCode)) {
            throw new NotFoundException("reference.district.not_found_by_code", parentCode);
        }
    }

    private int sortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }
}
