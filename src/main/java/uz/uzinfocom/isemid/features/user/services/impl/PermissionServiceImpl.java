package uz.uzinfocom.isemid.features.user.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.user.dtos.requests.PermissionRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.PermissionResponse;
import uz.uzinfocom.isemid.features.user.mappers.PermissionMapper;
import uz.uzinfocom.isemid.features.user.models.Permission;
import uz.uzinfocom.isemid.features.user.reposiotries.PermissionRepository;
import uz.uzinfocom.isemid.features.user.services.PermissionService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.utils.MessageUtil.*;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public ApiResponse<PermissionResponse> create(PermissionRequest permissionRequest) {
        Permission permission = Permission.builder()
                .subject(permissionRequest.getSubject())
                .actions(permissionRequest.getActions())
                .descriptionUz(permissionRequest.getDescriptionUz())
                .descriptionUzCyril(permissionRequest.getDescriptionUzCyril())
                .descriptionRu(permissionRequest.getDescriptionRu())
                .descriptionKaa(permissionRequest.getDescriptionKaa())
                .build();
        return new ApiResponse<>(true, CREATED_SUCCESS, permissionMapper.toResponse(permissionRepository.save(permission)));
    }

    @Override
    public ApiResponse<PermissionResponse> getBydId(long id) {
        return new ApiResponse<>(true, FETCHED_SUCCESS,
                permissionMapper.toResponse(permissionRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND))));
    }

    @Override
    public ApiResponse<PermissionResponse> update(long id, PermissionRequest pmRequest) {
        Permission exsistingPermission = permissionRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));
        Optional.ofNullable(pmRequest.getSubject()).ifPresent(exsistingPermission::setSubject);
        Optional.ofNullable(pmRequest.getActions()).ifPresent(exsistingPermission::setActions);
        Optional.ofNullable(pmRequest.getActive()).ifPresent(exsistingPermission::setActive);
        Optional.ofNullable(pmRequest.getDescriptionUz()).ifPresent(exsistingPermission::setDescriptionUz);
        Optional.ofNullable(pmRequest.getDescriptionRu()).ifPresent(exsistingPermission::setDescriptionRu);
        Optional.ofNullable(pmRequest.getDescriptionUzCyril()).ifPresent(exsistingPermission::setDescriptionUzCyril);
        Optional.ofNullable(pmRequest.getDescriptionKaa()).ifPresent(exsistingPermission::setDescriptionKaa);
        return new ApiResponse<>(true, UPDATED_SUCCESS, permissionMapper.toResponse(permissionRepository.save(exsistingPermission)));
    }

    @Override
    public ApiResponse<?> deleteById(long id) {
        try {
            permissionRepository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, MessageUtil.INTERNAL_SERVER_ERROR, e);
        }
        return new ApiResponse<>(true, DELETED_SUCCESS, null);
    }
}
