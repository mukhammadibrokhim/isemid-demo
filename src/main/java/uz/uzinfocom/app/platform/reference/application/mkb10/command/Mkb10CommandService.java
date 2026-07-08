package uz.uzinfocom.app.platform.reference.application.mkb10.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.reference.application.common.ReferenceCodeNormalizer;
import uz.uzinfocom.app.platform.reference.application.common.event.Mkb10ChangedEvent;
import uz.uzinfocom.app.platform.reference.application.mkb10.dto.Mkb10CreateRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.dto.Mkb10UpdateRequest;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10Response;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.mapper.Mkb10Mapper;
import uz.uzinfocom.app.platform.reference.domain.Mkb10;
import uz.uzinfocom.app.platform.reference.repository.Mkb10Repository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class Mkb10CommandService {

    private final Mkb10Repository mkb10Repository;
    private final Mkb10Mapper mkb10Mapper;
    private final Mkb10CommandMapper mkb10CommandMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Mkb10Response create(Mkb10CreateRequest request) {
        if (mkb10Repository.existsById(request.id())) {
            throw new ConflictException("reference.mkb10.id.already_exists", request.id());
        }

        String code = normalizedCode(request.code());
        if (mkb10Repository.existsByCode(code)) {
            throw new ConflictException("reference.mkb10.code.already_exists", code);
        }

        if (request.parentId() != null && !mkb10Repository.existsById(request.parentId())) {
            throw new NotFoundException("reference.mkb10.not_found_by_id", request.parentId());
        }

        Mkb10 mkb10 = mkb10CommandMapper.toEntity(request);

        Mkb10 saved = mkb10Repository.save(mkb10);
        eventPublisher.publishEvent(new Mkb10ChangedEvent());
        log.debug("Reference MKB-10 node created. id={}, code={}", saved.getId(), saved.getCode());

        return toResponse(saved);
    }

    @Transactional
    public Mkb10Response update(Long id, Mkb10UpdateRequest request) {
        Mkb10 mkb10 = mkb10Repository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.mkb10.not_found_by_id", id));

        if (mkb10.isDeleted()) {
            throw new ConflictException("reference.mkb10.update.deleted_conflict", id);
        }

        String code = normalizedCode(request.code());
        if (!Objects.equals(mkb10.getCode(), code) && mkb10Repository.existsByCode(code)) {
            throw new ConflictException("reference.mkb10.code.already_exists", code);
        }

        if (request.parentId() != null) {
            if (Objects.equals(request.parentId(), id)) {
                throw new ConflictException("reference.mkb10.parent_id.self_reference", id);
            }
            if (!mkb10Repository.existsById(request.parentId())) {
                throw new NotFoundException("reference.mkb10.not_found_by_id", request.parentId());
            }
        }

        mkb10CommandMapper.updateEntity(mkb10, request);

        Mkb10 saved = mkb10Repository.save(mkb10);
        eventPublisher.publishEvent(new Mkb10ChangedEvent());
        log.debug("Reference MKB-10 node updated. id={}, code={}", saved.getId(), saved.getCode());

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Mkb10 mkb10 = mkb10Repository.findById(id)
                .orElseThrow(() -> new NotFoundException("reference.mkb10.not_found_by_id", id));

        if (mkb10.isDeleted()) {
            return;
        }

        if (mkb10Repository.countByParent_IdAndDeletedFalse(id) > 0) {
            throw new ConflictException("reference.mkb10.delete.has_children", id);
        }

        mkb10.setDeleted(true);
        mkb10Repository.save(mkb10);
        eventPublisher.publishEvent(new Mkb10ChangedEvent());
        log.debug("Reference MKB-10 node soft-deleted. id={}, code={}", mkb10.getId(), mkb10.getCode());
    }

    private String normalizedCode(String code) {
        return ReferenceCodeNormalizer.normalizeCode(code).toUpperCase(Locale.ROOT);
    }

    private Mkb10Response toResponse(Mkb10 saved) {
        long childrenCount = mkb10Repository.countByParent_IdAndDeletedFalse(saved.getId());
        return mkb10Mapper.toResponse(saved, childrenCount);
    }
}
