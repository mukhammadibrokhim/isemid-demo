package uz.uzinfocom.app.platform.iam.application.action.command;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.application.action.command.dto.ActionCreateRequest;
import uz.uzinfocom.app.platform.iam.application.action.command.dto.ActionUpdateRequest;
import uz.uzinfocom.app.platform.iam.application.action.query.dto.ActionTableResponse;
import uz.uzinfocom.app.platform.iam.application.action.query.mapper.ActionQueryMapper;
import uz.uzinfocom.app.platform.iam.domain.Action;
import uz.uzinfocom.app.platform.iam.repository.ActionRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.time.LocalDateTime;

@Service
@CacheConfig(cacheManager = "securityCacheManager")
@RequiredArgsConstructor
public class ActionCommandService {

    private final ActionRepository actionRepository;
    private final ActionQueryMapper actionQueryMapper;

    @Transactional
    public ActionTableResponse create(ActionCreateRequest request) {
        String code = normalizeCode(request.code());

        if (actionRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("action.code.already_exists", code);
        }

        Action action = Action.builder()
                .code(code)
                .descriptionUz(request.descriptionUz())
                .descriptionRu(request.descriptionRu())
                .descriptionUzCyril(request.descriptionUzCyril())
                .descriptionKaa(request.descriptionKaa())
                .active(request.active() == null || Boolean.TRUE.equals(request.active()))
                .deleted(false)
                .build();

        Action saved = actionRepository.save(action);
        return actionQueryMapper.toTableResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public ActionTableResponse update(Long id, ActionUpdateRequest request) {
        Action action = actionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("action.not_found_by_id", id));

        if (Boolean.TRUE.equals(action.getDeleted())) {
            throw new ConflictException("action.update.deleted_conflict", id);
        }

        String code = normalizeCode(request.code());

        if (actionRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new ConflictException("action.code.already_exists", code);
        }

        action.setCode(code);
        action.setDescriptionUz(request.descriptionUz());
        action.setDescriptionRu(request.descriptionRu());
        action.setDescriptionUzCyril(request.descriptionUzCyril());
        action.setDescriptionKaa(request.descriptionKaa());
        action.setActive(request.active());

        Action saved = actionRepository.save(action);

        return actionQueryMapper.toTableResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public void delete(Long id) {
        Action action = actionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("action.not_found_by_id", id));

        if (Boolean.TRUE.equals(action.getDeleted())) {
            return;
        }

        action.setActive(false);
        action.setDeleted(true);
        action.setDeletedAt(LocalDateTime.now());

        actionRepository.save(action);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID, allEntries = true),
            @CacheEvict(cacheNames = SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS, allEntries = true)
    })
    public void restore(Long id) {
        Action action = actionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("action.not_found_by_id", id));

        if (Boolean.FALSE.equals(action.getDeleted())) {
            return;
        }

        action.setDeleted(false);
        action.setDeletedAt(null);
        action.setActive(true);

        actionRepository.save(action);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ConflictException("action.code.required");
        }
        return code.trim().toUpperCase();
    }
}
