package uz.uzinfocom.app.platform.reference.application.country.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.common.event.CountryChangedEvent;
import uz.uzinfocom.app.platform.reference.domain.Country;
import uz.uzinfocom.app.platform.reference.application.country.dto.CountryCreateRequest;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryResponse;
import uz.uzinfocom.app.platform.reference.application.country.dto.CountryUpdateRequest;
import uz.uzinfocom.app.platform.reference.application.country.query.mapper.CountryMapper;
import uz.uzinfocom.app.platform.reference.repository.CountryRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryCommandService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
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
                .build();

        Country saved = countryRepository.save(country);
        eventPublisher.publishEvent(new CountryChangedEvent());
        log.debug("Reference country created. id={}, code={}", saved.getId(), saved.getCode());

        return countryMapper.toResponse(saved);
    }

    @Transactional
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

        Country saved = countryRepository.save(country);
        eventPublisher.publishEvent(new CountryChangedEvent());
        log.debug("Reference country updated. id={}, code={}", saved.getId(), saved.getCode());

        return countryMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.country.not_found_by_id", id));

        if (country.isDeleted()) {
            return;
        }

        country.setDeleted(true);
        countryRepository.save(country);
        eventPublisher.publishEvent(new CountryChangedEvent());
        log.debug("Reference country soft-deleted. id={}, code={}", country.getId(), country.getCode());
    }

}
