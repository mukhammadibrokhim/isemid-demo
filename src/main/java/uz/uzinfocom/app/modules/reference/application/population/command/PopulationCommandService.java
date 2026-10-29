package uz.uzinfocom.app.modules.reference.application.population.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.application.population.dto.PopulationCreateRequest;
import uz.uzinfocom.app.modules.reference.application.population.dto.PopulationUpdateRequest;
import uz.uzinfocom.app.modules.reference.application.population.query.PopulationQueryService;
import uz.uzinfocom.app.modules.reference.application.population.query.dto.PopulationDetailResponse;
import uz.uzinfocom.app.modules.reference.domain.Population;
import uz.uzinfocom.app.modules.reference.repository.PopulationRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopulationCommandService {

    private final PopulationRepository populationRepository;
    private final PopulationCommandMapper populationCommandMapper;
    private final PopulationQueryService populationQueryService;

    @Transactional
    public PopulationDetailResponse create(PopulationCreateRequest request) {
        if (populationRepository.existsBySoatoIdAndYearAndDeletedFalse(request.soatoId(), request.year())) {
            throw new ConflictException(
                    "reference.population.already_exists", request.soatoId(), request.year()
            );
        }

        Population saved = populationRepository.save(populationCommandMapper.toEntity(request));
        log.debug("Reference population created. id={}, soatoId={}, year={}",
                saved.getId(), saved.getSoatoId(), saved.getYear());

        return populationQueryService.getById(saved.getId());
    }

    @Transactional
    public PopulationDetailResponse update(Long id, PopulationUpdateRequest request) {
        Population population = populationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.population.not_found_by_id", id));

        if (population.isDeleted()) {
            throw new ConflictException("reference.population.update.deleted_conflict", id);
        }

        populationCommandMapper.updateEntity(population, request);
        Population saved = populationRepository.save(population);
        log.debug("Reference population updated. id={}, soatoId={}, year={}",
                saved.getId(), saved.getSoatoId(), saved.getYear());

        return populationQueryService.getById(saved.getId());
    }

    @Transactional
    public void delete(Long id) {
        Population population = populationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.population.not_found_by_id", id));

        if (population.isDeleted()) {
            return;
        }

        population.setDeleted(true);
        populationRepository.save(population);
        log.debug("Reference population soft-deleted. id={}", population.getId());
    }
}
