package uz.uzinfocom.app.platform.reference.application.neighborhood.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.config.ReferenceCacheConfig;
import uz.uzinfocom.app.platform.reference.domain.Neighborhood;
import uz.uzinfocom.app.platform.reference.application.neighborhood.dto.NeighborhoodCreateRequest;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.dto.NeighborhoodUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.mapper.NeighborhoodMapper;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.NeighborhoodRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Objects;

@Slf4j
@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class NeighborhoodCommandService {

    private final NeighborhoodRepository neighborhoodRepository;
    private final DistrictRepository districtRepository;
    private final NeighborhoodMapper neighborhoodMapper;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOODS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOOD_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOODS_BY_PARENT_CODE, allEntries = true)
    })
    public NeighborhoodResponse create(NeighborhoodCreateRequest request) {
        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (neighborhoodRepository.existsByCode(code)) {
            throw new ConflictException("reference.mahalla.code.already_exists", code);
        }

        validateDistrictParent(parentCode);

        Neighborhood neighborhood = Neighborhood.builder()
                .code(code)
                .parentCode(parentCode)
                .nameUz(request.nameUz())
                .nameUzCyril(request.nameUzCyril())
                .nameRu(request.nameRu())
                .nameKaa(request.nameKaa())
                .deleted(false)
                .sortOrder(sortOrder(request.sortOrder()))
                .build();

        Neighborhood saved = neighborhoodRepository.save(neighborhood);
        log.debug("Reference neighborhood created. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return neighborhoodMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOODS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOOD_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOODS_BY_PARENT_CODE, allEntries = true)
    })
    public NeighborhoodResponse update(Long id, NeighborhoodUpdateRequest request) {
        Neighborhood neighborhood = neighborhoodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.mahalla.not_found_by_id", id));

        if (neighborhood.isDeleted()) {
            throw new ConflictException("reference.mahalla.update.deleted_conflict", id);
        }

        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (!Objects.equals(neighborhood.getCode(), code) && neighborhoodRepository.existsByCode(code)) {
            throw new ConflictException("reference.mahalla.code.already_exists", code);
        }

        validateDistrictParent(parentCode);

        neighborhood.setCode(code);
        neighborhood.setParentCode(parentCode);
        neighborhood.setNameUz(request.nameUz());
        neighborhood.setNameUzCyril(request.nameUzCyril());
        neighborhood.setNameRu(request.nameRu());
        neighborhood.setNameKaa(request.nameKaa());
        neighborhood.setSortOrder(sortOrder(request.sortOrder()));

        Neighborhood saved = neighborhoodRepository.save(neighborhood);
        log.debug("Reference neighborhood updated. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return neighborhoodMapper.toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOODS, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOOD_BY_CODE, allEntries = true),
            @CacheEvict(cacheNames = ReferenceCacheConfig.REF_NEIGHBORHOODS_BY_PARENT_CODE, allEntries = true)
    })
    public void delete(Long id) {
        Neighborhood neighborhood = neighborhoodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.mahalla.not_found_by_id", id));

        if (neighborhood.isDeleted()) {
            return;
        }

        neighborhood.setDeleted(true);
        neighborhoodRepository.save(neighborhood);
        log.debug("Reference neighborhood soft-deleted. id={}, code={}, parentCode={}",
                neighborhood.getId(), neighborhood.getCode(), neighborhood.getParentCode());
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
