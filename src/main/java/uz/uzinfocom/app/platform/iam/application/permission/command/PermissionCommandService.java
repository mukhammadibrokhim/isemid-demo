package uz.uzinfocom.app.platform.iam.application.permission.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.exception.ConflictException;
import uz.uzinfocom.app.platform.exception.NotFoundException;
import uz.uzinfocom.app.platform.iam.application.permission.command.dto.PermissionCreateRequest;
import uz.uzinfocom.app.platform.iam.application.permission.command.dto.PermissionUpdateRequest;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionTableResponse;
import uz.uzinfocom.app.platform.iam.application.permission.query.mapper.PermissionQueryMapper;
import uz.uzinfocom.app.platform.iam.domain.Permission;
import uz.uzinfocom.app.platform.iam.repository.PermissionRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PermissionCommandService {

    private final PermissionRepository permissionRepository;
    private final PermissionQueryMapper permissionQueryMapper;

    @Transactional
    public PermissionTableResponse create(PermissionCreateRequest request) {
        String subject = normalizeSubject(request.subject());

        if (permissionRepository.existsBySubjectIgnoreCase(subject)) {
            throw new ConflictException("permission.subject.already.exists", subject);
        }

        Permission permission = Permission.builder()
                .subject(subject)
                .descriptionUz(request.descriptionUz())
                .descriptionRu(request.descriptionRu())
                .descriptionUzCyril(request.descriptionUzCyril())
                .descriptionKaa(request.descriptionKaa())
                .active(request.active() == null || Boolean.TRUE.equals(request.active()))
                .deleted(false)
                .build();

        Permission saved = permissionRepository.save(permission);
        return permissionQueryMapper.toTableResponse(saved);
    }

    @Transactional
    public PermissionTableResponse update(Long id, PermissionUpdateRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("permission.not.found", id));

        if (Boolean.TRUE.equals(permission.getDeleted())) {
            throw new ConflictException("permission.update.deleted.conflict", id);
        }

        String subject = normalizeSubject(request.subject());

        if (permissionRepository.existsBySubjectIgnoreCaseAndIdNot(subject, id)) {
            throw new ConflictException("permission.subject.already.exists", subject);
        }

        permission.setSubject(subject);
        permission.setDescriptionUz(request.descriptionUz());
        permission.setDescriptionRu(request.descriptionRu());
        permission.setDescriptionUzCyril(request.descriptionUzCyril());
        permission.setDescriptionKaa(request.descriptionKaa());
        permission.setActive(request.active());

        Permission saved = permissionRepository.save(permission);

        return permissionQueryMapper.toTableResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("permission.not.found", id));

        if (Boolean.TRUE.equals(permission.getDeleted())) {
            return;
        }

        permission.setActive(false);
        permission.setDeleted(true);
        permission.setDeletedAt(LocalDateTime.now());

        permissionRepository.save(permission);
    }

    @Transactional
    public void restore(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("permission.not.found", id));

        if (Boolean.FALSE.equals(permission.getDeleted())) {
            return;
        }

        permission.setDeleted(false);
        permission.setDeletedAt(null);
        permission.setActive(true);

        permissionRepository.save(permission);
    }

    private String normalizeSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new ConflictException("permission.subject.required");
        }
        return subject.trim();
    }
}
