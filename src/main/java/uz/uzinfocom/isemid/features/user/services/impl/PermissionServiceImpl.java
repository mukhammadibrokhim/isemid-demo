package uz.uzinfocom.isemid.features.user.services.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.user.dtos.requests.PermissionRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.PermissionResponse;
import uz.uzinfocom.isemid.features.user.mappers.PermissionMapper;
import uz.uzinfocom.isemid.features.user.models.Permission;
import uz.uzinfocom.isemid.features.user.reposiotries.PermissionRepository;
import uz.uzinfocom.isemid.features.user.services.PermissionService;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.utils.MessageUtil.*;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.commons.utils.PageableUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public ApiResponsePage getAllPermissionsPage(int page, int size, String subject, String descriptionUz, String descriptionRu, String descriptionUzCyril, String descriptionKaa, Boolean active, String url) {
        Specification<Permission> spec = getFilteredSpecification(subject, descriptionUz, descriptionRu, descriptionUzCyril, descriptionKaa, active);
        Page<Permission> rolePage = permissionRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, rolePage.map(permissionMapper::toResponse), url);
    }

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


    private static Specification<Permission> getFilteredSpecification(
            String subject,
            String descriptionUz,
            String descriptionRu,
            String descriptionUzCyril,
            String descriptionKaa,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                                hasText(subject) ? criteriaBuilder.like(root.get("subject"), "%" + subject + "%") : null,
                                hasText(descriptionUz) ? criteriaBuilder.like(root.get("descriptionUz"), "%" + descriptionUz + "%") : null,
                                hasText(descriptionRu) ? criteriaBuilder.like(root.get("descriptionRu"), "%" + descriptionRu + "%") : null,
                                hasText(descriptionUzCyril) ? criteriaBuilder.like(root.get("descriptionUzCyril"), "%" + descriptionUzCyril + "%") : null,
                                hasText(descriptionKaa) ? criteriaBuilder.like(root.get("descriptionKaa"), "%" + descriptionKaa + "%") : null,
                                active != null ? criteriaBuilder.equal(root.get("active"), active) : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }
}
