package uz.uzinfocom.app.modules.reference.application.icd10.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.modules.reference.application.common.event.Icd10ChangedEvent;
import uz.uzinfocom.app.modules.reference.application.icd10.dto.Icd10CreateRequest;
import uz.uzinfocom.app.modules.reference.application.icd10.dto.Icd10UpdateRequest;
import uz.uzinfocom.app.modules.reference.application.icd10.query.dto.Icd10Response;
import uz.uzinfocom.app.modules.reference.application.icd10.query.mapper.Icd10Mapper;
import uz.uzinfocom.app.modules.reference.domain.Icd10;
import uz.uzinfocom.app.modules.reference.repository.Icd10Repository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class Icd10CommandService {

    private final Icd10Repository icd10Repository;
    private final Icd10Mapper icd10Mapper;
    private final Icd10CommandMapper icd10CommandMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Icd10Response create(Icd10CreateRequest request) {
        if (icd10Repository.existsById(request.id())) {
            throw new ConflictException("reference.icd10.id.already_exists", request.id());
        }

        String code = normalizedCode(request.code());
        if (icd10Repository.existsByCode(code)) {
            throw new ConflictException("reference.icd10.code.already_exists", code);
        }

        if (request.parentId() != null && !icd10Repository.existsById(request.parentId())) {
            throw new NotFoundException("reference.icd10.not_found_by_id", request.parentId());
        }

        Icd10 icd10 = icd10CommandMapper.toEntity(request);

        Icd10 saved = icd10Repository.save(icd10);
        eventPublisher.publishEvent(new Icd10ChangedEvent());
        log.debug("Reference MKB-10 node created. id={}, code={}", saved.getId(), saved.getCode());

        return toResponse(saved);
    }

    @Transactional
    public Icd10Response update(Long id, Icd10UpdateRequest request) {
        Icd10 icd10 = icd10Repository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.icd10.not_found_by_id", id));

        if (icd10.isDeleted()) {
            throw new ConflictException("reference.icd10.update.deleted_conflict", id);
        }

        String code = normalizedCode(request.code());
        if (!Objects.equals(icd10.getCode(), code) && icd10Repository.existsByCode(code)) {
            throw new ConflictException("reference.icd10.code.already_exists", code);
        }

        if (request.parentId() != null) {
            if (Objects.equals(request.parentId(), id)) {
                throw new ConflictException("reference.icd10.parent_id.self_reference", id);
            }
            if (!icd10Repository.existsById(request.parentId())) {
                throw new NotFoundException("reference.icd10.not_found_by_id", request.parentId());
            }
        }

        icd10CommandMapper.updateEntity(icd10, request);

        Icd10 saved = icd10Repository.save(icd10);
        eventPublisher.publishEvent(new Icd10ChangedEvent());
        log.debug("Reference MKB-10 node updated. id={}, code={}", saved.getId(), saved.getCode());

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Icd10 icd10 = icd10Repository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.icd10.not_found_by_id", id));

        if (icd10.isDeleted()) {
            return;
        }

        if (icd10Repository.countByParent_IdAndDeletedFalse(id) > 0) {
            throw new ConflictException("reference.icd10.delete.has_children", id);
        }

        icd10.setDeleted(true);
        icd10Repository.save(icd10);
        eventPublisher.publishEvent(new Icd10ChangedEvent());
        log.debug("Reference MKB-10 node soft-deleted. id={}, code={}", icd10.getId(), icd10.getCode());
    }

    private String normalizedCode(String code) {
        return ReferenceCodeNormalizer.normalizeCode(code).toUpperCase(Locale.ROOT);
    }

    private Icd10Response toResponse(Icd10 saved) {
        long childrenCount = icd10Repository.countByParent_IdAndDeletedFalse(saved.getId());
        return icd10Mapper.toResponse(saved, childrenCount);
    }
}
