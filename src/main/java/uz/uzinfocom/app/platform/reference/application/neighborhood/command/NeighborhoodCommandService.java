package uz.uzinfocom.app.platform.reference.application.neighborhood.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.common.event.NeighborhoodChangedEvent;
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
@RequiredArgsConstructor
public class NeighborhoodCommandService {

    private final NeighborhoodRepository neighborhoodRepository;
    private final DistrictRepository districtRepository;
    private final NeighborhoodMapper neighborhoodMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public NeighborhoodResponse create(NeighborhoodCreateRequest request) {
        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (neighborhoodRepository.existsByCode(code)) {
            throw new ConflictException("reference.neighborhood.code.already_exists", code);
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
        eventPublisher.publishEvent(new NeighborhoodChangedEvent());
        log.debug("Reference neighborhood created. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return neighborhoodMapper.toResponse(saved);
    }

    @Transactional
    public NeighborhoodResponse update(Long id, NeighborhoodUpdateRequest request) {
        Neighborhood neighborhood = neighborhoodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.neighborhood.not_found_by_id", id));

        if (neighborhood.isDeleted()) {
            throw new ConflictException("reference.neighborhood.update.deleted_conflict", id);
        }

        String code = ReferenceCodeNormalizer.normalizeCode(request.code());
        String parentCode = ReferenceCodeNormalizer.normalizeParentCode(request.parentCode());

        if (!Objects.equals(neighborhood.getCode(), code) && neighborhoodRepository.existsByCode(code)) {
            throw new ConflictException("reference.neighborhood.code.already_exists", code);
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
        eventPublisher.publishEvent(new NeighborhoodChangedEvent());
        log.debug("Reference neighborhood updated. id={}, code={}, parentCode={}",
                saved.getId(), saved.getCode(), saved.getParentCode());

        return neighborhoodMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Neighborhood neighborhood = neighborhoodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.neighborhood.not_found_by_id", id));

        if (neighborhood.isDeleted()) {
            return;
        }

        neighborhood.setDeleted(true);
        neighborhoodRepository.save(neighborhood);
        eventPublisher.publishEvent(new NeighborhoodChangedEvent());
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
