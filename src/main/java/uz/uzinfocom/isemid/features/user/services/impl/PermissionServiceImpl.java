package uz.uzinfocom.isemid.features.user.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.user.dtos.requests.PermissionRequest;
import uz.uzinfocom.isemid.features.user.mappers.PermissionMapper;
import uz.uzinfocom.isemid.features.user.models.Permission;
import uz.uzinfocom.isemid.features.user.reposiotries.PermissionRepository;
import uz.uzinfocom.isemid.features.user.services.PermissionService;
import uz.uzinfocom.isemid.features.user.specifications.PermissionSpecification;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<Permission> filter(int page, int size, String subject, String descriptionUz, String descriptionRu, String descriptionUzCyril, String descriptionKaa, Boolean active) {
        Specification<Permission> spec = PermissionSpecification.getFilteredSpecification(subject, descriptionUz, descriptionRu, descriptionUzCyril, descriptionKaa, active);
        return permissionRepository.findAll(spec, pageableSortByIdDesc(page, size));
    }

    @Override
    public Permission create(PermissionRequest request) {
        Permission permission = permissionMapper.toEntity(request);
        return permissionRepository.save(permission);
    }

    @Override
    public Permission getBydId(long id) {
        return permissionRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public Permission update(long id, PermissionRequest pmRequest) {
        Permission exsistingPermission = permissionRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        permissionMapper.updatePermission(pmRequest, exsistingPermission);
        return permissionRepository.save(exsistingPermission);
    }

    @Override
    public void deleteById(long id) {
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
        try {
            permissionRepository.delete(permission);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("error.internal_server"), e);
        }
    }
}
