package uz.uzinfocom.app.platform.devmonitoring.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevErrorDetailResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevErrorFilterRequest;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevErrorResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.specification.DevErrorLogSpecification;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorLog;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevErrorLogRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

@Service
@RequiredArgsConstructor
public class DevErrorQueryService {

    private final DevErrorLogRepository devErrorLogRepository;

    @Transactional(readOnly = true)
    public Page<DevErrorResponse> findAll(DevErrorFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, DevErrorSortFields.ALLOWED);

        return devErrorLogRepository
                .findAll(DevErrorLogSpecification.byFilter(request), pageable)
                .map(DevErrorQueryService::toResponse);
    }

    @Transactional(readOnly = true)
    public DevErrorDetailResponse findById(Long id) {
        DevErrorLog entity = devErrorLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("dev-monitoring.error.not-found", id));

        return toDetailResponse(entity);
    }

    private static DevErrorResponse toResponse(DevErrorLog entity) {
        return new DevErrorResponse(
                entity.getId(),
                entity.getErrorCode(),
                entity.getHttpStatus(),
                entity.getExceptionType(),
                entity.getPath(),
                entity.getMethod(),
                entity.getOccurredAt(),
                entity.getStatus()
        );
    }

    private static DevErrorDetailResponse toDetailResponse(DevErrorLog entity) {
        return new DevErrorDetailResponse(
                entity.getId(),
                entity.getTraceId(),
                entity.getErrorCode(),
                entity.getHttpStatus(),
                entity.getExceptionType(),
                entity.getPath(),
                entity.getMethod(),
                entity.getPrincipal(),
                entity.getMessage(),
                entity.getOccurredAt(),
                entity.getStatus(),
                entity.getResolvedBy(),
                entity.getResolvedAt()
        );
    }
}
