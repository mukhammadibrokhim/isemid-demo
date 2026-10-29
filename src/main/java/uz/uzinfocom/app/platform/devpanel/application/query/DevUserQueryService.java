package uz.uzinfocom.app.platform.devpanel.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevUserDetailResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevUserFilterRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.specification.DevUserSpecification;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;
import uz.uzinfocom.app.platform.devpanel.repository.DevUserRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

@Service
@RequiredArgsConstructor
public class DevUserQueryService {

    private final DevUserRepository devUserRepository;

    @Transactional(readOnly = true)
    public Page<DevUserDetailResponse> findAll(DevUserFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, DevUserSortFields.ALLOWED);

        return devUserRepository
                .findAll(DevUserSpecification.byFilter(request), pageable)
                .map(DevUserQueryService::toDetailResponse);
    }

    @Transactional(readOnly = true)
    public DevUserDetailResponse getById(Long id) {
        return toDetailResponse(findEntity(id));
    }

    public DevUser findEntity(Long id) {
        return devUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("dev-user.not-found", id));
    }

    public static DevUserDetailResponse toDetailResponse(DevUser devUser) {
        return new DevUserDetailResponse(
                devUser.getId(),
                devUser.getUsername(),
                devUser.isEnabled(),
                devUser.getRole(),
                devUser.isMustChangePassword(),
                devUser.getEmail(),
                devUser.getFullName(),
                devUser.getPhone(),
                devUser.getPosition() != null ? devUser.getPosition().getId() : null,
                devUser.getPosition() != null ? devUser.getPosition().getName() : null,
                devUser.getCreatedAt(),
                devUser.getUpdatedAt()
        );
    }
}
