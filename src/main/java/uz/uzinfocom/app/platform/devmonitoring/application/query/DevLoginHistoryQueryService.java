package uz.uzinfocom.app.platform.devmonitoring.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevLoginHistoryDetailResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevLoginHistoryFilterRequest;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevLoginHistoryResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.specification.DevLoginHistorySpecification;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevLoginHistory;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevLoginHistoryRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

@Service
@RequiredArgsConstructor
public class DevLoginHistoryQueryService {

    private final DevLoginHistoryRepository devLoginHistoryRepository;

    @Transactional(readOnly = true)
    public Page<DevLoginHistoryResponse> findAll(DevLoginHistoryFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, DevLoginHistorySortFields.ALLOWED);

        return devLoginHistoryRepository
                .findAll(DevLoginHistorySpecification.byFilter(request), pageable)
                .map(DevLoginHistoryQueryService::toResponse);
    }

    @Transactional(readOnly = true)
    public DevLoginHistoryDetailResponse findById(Long id) {
        DevLoginHistory entity = devLoginHistoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("dev-monitoring.login.not-found", id));

        return toDetailResponse(entity);
    }

    private static DevLoginHistoryResponse toResponse(DevLoginHistory entity) {
        return new DevLoginHistoryResponse(
                entity.getId(),
                entity.getProvider(),
                entity.getUsername(),
                entity.getUserId(),
                entity.getSuccess(),
                entity.getIp(),
                entity.getOccurredAt()
        );
    }

    private static DevLoginHistoryDetailResponse toDetailResponse(DevLoginHistory entity) {
        return new DevLoginHistoryDetailResponse(
                entity.getId(),
                entity.getProvider(),
                entity.getUsername(),
                entity.getUserId(),
                entity.getSuccess(),
                entity.getFailureReason(),
                entity.getIp(),
                entity.getUserAgent(),
                entity.getTraceId(),
                entity.getOccurredAt()
        );
    }
}
