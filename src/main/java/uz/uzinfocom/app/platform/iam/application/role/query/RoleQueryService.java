package uz.uzinfocom.app.platform.iam.application.role.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.exception.NotFoundException;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionDetailResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleDetailResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleFilterRequest;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleTableResponse;
import uz.uzinfocom.app.platform.iam.application.role.query.mapper.RoleQueryMapper;
import uz.uzinfocom.app.platform.iam.application.role.query.projection.RoleTableProjection;
import uz.uzinfocom.app.platform.iam.application.role.query.specification.RoleSpecification;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;
import uz.uzinfocom.app.platform.web.pagination.PageableUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleQueryService {

    private final RoleRepository roleRepo;
    private final RoleQueryMapper roleQueryMapper;

    public Page<RoleTableResponse> findTable(RoleFilterRequest request) {
        Pageable pageable = PageableUtils.of(
                request,
                RoleSortFields.ALLOWED
        );

        Page<RoleTableProjection> page = roleRepo.findBy(
                RoleSpecification.byFilter(request),
                query -> query
                        .as(RoleTableProjection.class)
                        .page(pageable)
        );

        assert page != null;
        return page.map(roleQueryMapper::toTableResponse);
    }

    public RoleDetailResponse findDetail(Long id) {
        Role role = roleRepo.findWithPermissionsById(id)
                .orElseThrow(() -> new NotFoundException("role.not.found", id));
        return roleQueryMapper.toDetailResponse(role);
    }

    public List<PermissionDetailResponse> findPermissions(Long id) {
        return null;
    }
}
