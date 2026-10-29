package uz.uzinfocom.app.platform.devpanel.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevPositionFilterRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevPositionResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.specification.DevPositionSpecification;
import uz.uzinfocom.app.platform.devpanel.domain.DevPosition;
import uz.uzinfocom.app.platform.devpanel.repository.DevPositionRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

@Service
@RequiredArgsConstructor
public class DevPositionQueryService {

    private final DevPositionRepository devPositionRepository;

    @Transactional(readOnly = true)
    public Page<DevPositionResponse> findAll(DevPositionFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, DevPositionSortFields.ALLOWED);

        return devPositionRepository
                .findAll(DevPositionSpecification.byFilter(request), pageable)
                .map(DevPositionQueryService::toResponse);
    }

    @Transactional(readOnly = true)
    public DevPositionResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public DevPosition findEntity(Long id) {
        return devPositionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("dev-position.not-found", id));
    }

    private static DevPositionResponse toResponse(DevPosition devPosition) {
        return new DevPositionResponse(
                devPosition.getId(),
                devPosition.getName(),
                devPosition.isEnabled(),
                devPosition.getCreatedAt(),
                devPosition.getUpdatedAt()
        );
    }
}
