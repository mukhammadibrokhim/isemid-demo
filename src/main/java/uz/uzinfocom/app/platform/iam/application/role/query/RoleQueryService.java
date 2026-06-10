package uz.uzinfocom.app.platform.iam.application.role.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleDetailResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleFilterRequest;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RolePermissionResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleTableResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.mapper.RoleQueryMapper;
import uz.uzinfocom.app.platform.iam.application.role.query.mapper.helper.RolePermissionQueryMappingHelper;
import uz.uzinfocom.app.platform.iam.application.role.query.projection.RoleTableProjection;
import uz.uzinfocom.app.platform.iam.application.role.query.specification.RoleSpecification;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleQueryService {

    private final RoleRepository roleRepo;
    private final RoleQueryMapper roleQueryMapper;
    private final RolePermissionQueryMappingHelper rolePermissionQueryMappingHelper;
    private final AuditResolver auditResolver;

    @Transactional(readOnly = true)
    public Page<RoleTableResponse> findTable(RoleFilterRequest request) {
        Pageable pageable = PageableUtils.of(
                request,
                RoleSortFields.ALLOWED
        );

        Page<RoleTableProjection> page = Objects.requireNonNull(roleRepo.findBy(
                RoleSpecification.byFilter(request),
                query -> query
                        .as(RoleTableProjection.class)
                        .page(pageable)), "Role Table page return null"
        );

        return page.map(roleQueryMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public RoleDetailResponse findDetail(Long id) {
        Role role = roleRepo.findWithPermissionsById(id)
                .orElseThrow(() -> new NotFoundException("role.not_found", id));
        return roleQueryMapper.toDetailResponse(role, auditResolver.resolve(role));
    }

    @Transactional(readOnly = true)
    public List<RolePermissionResponse> findPermissions(Long id) {
        Role role = roleRepo.findWithPermissionsById(id).orElseThrow(() -> new NotFoundException("role.not_found", id));
        return rolePermissionQueryMappingHelper.toRolePermissionResponses(role.getRolePermissions());
    }
}
