package uz.uzinfocom.app.platform.iam.application.permission.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionDetailResponse;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionFilterRequest;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionTableResponse;
import uz.uzinfocom.app.platform.iam.application.permission.query.mapper.PermissionQueryMapper;
import uz.uzinfocom.app.platform.iam.application.permission.query.projection.PermissionTableProjection;
import uz.uzinfocom.app.platform.iam.application.permission.query.specification.PermissionSpecification;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.iam.domain.Permission;
import uz.uzinfocom.app.platform.iam.repository.PermissionRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PermissionQueryService {

    private final PermissionRepository permRepo;
    private final PermissionQueryMapper permQueryMapper;
    private final AuditResolver auditResolver;

    @Transactional(readOnly = true)
    public Page<PermissionTableResponse> findTable(PermissionFilterRequest request) {
        Pageable pageable = PageableUtils.of(
                request,
                PermissionSortFields.ALLOWED
        );

        Page<PermissionTableProjection> page = Objects.requireNonNull(permRepo.findBy(
                PermissionSpecification.byFilter(request),
                query -> query
                        .as(PermissionTableProjection.class)
                        .page(pageable)
                ), "Permission table page return null"
        );

        return page.map(permQueryMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public PermissionDetailResponse getById(Long id) {
        Permission permission = permRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("permission.not_found_by_id", id));
        return permQueryMapper.toDetailResponse(permission, auditResolver.resolve(permission));
    }
}
