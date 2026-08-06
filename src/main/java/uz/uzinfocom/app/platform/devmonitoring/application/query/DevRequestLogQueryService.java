package uz.uzinfocom.app.platform.devmonitoring.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevRequestLogDetailResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevRequestLogFilterRequest;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevRequestLogResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.specification.DevRequestLogSpecification;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevRequestLog;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevRequestLogRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

@Service
@RequiredArgsConstructor
public class DevRequestLogQueryService {

    private final DevRequestLogRepository devRequestLogRepository;

    @Transactional(readOnly = true)
    public Page<DevRequestLogResponse> findAll(DevRequestLogFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, DevRequestLogSortFields.ALLOWED);

        return devRequestLogRepository
                .findAll(DevRequestLogSpecification.byFilter(request), pageable)
                .map(DevRequestLogQueryService::toResponse);
    }

    @Transactional(readOnly = true)
    public DevRequestLogDetailResponse findById(Long id) {
        DevRequestLog entity = devRequestLogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("dev-monitoring.request.not-found", id));

        return toDetailResponse(entity);
    }

    private static DevRequestLogResponse toResponse(DevRequestLog entity) {
        return new DevRequestLogResponse(
                entity.getId(),
                entity.getTraceId(),
                entity.getMethod(),
                entity.getPath(),
                entity.getHttpStatus(),
                entity.getOutcome(),
                entity.getDurationMs(),
                entity.getPrincipal(),
                entity.getOrganizationId(),
                entity.getClientIp(),
                entity.getOccurredAt()
        );
    }

    private static DevRequestLogDetailResponse toDetailResponse(DevRequestLog entity) {
        return new DevRequestLogDetailResponse(
                entity.getId(),
                entity.getTraceId(),
                entity.getMethod(),
                entity.getRoute(),
                entity.getPath(),
                entity.getQuery(),
                entity.getHttpStatus(),
                entity.getOutcome(),
                entity.getDurationMs(),
                entity.getClientIp(),
                entity.getPrincipal(),
                entity.getOrganizationId(),
                entity.getRequestContentType(),
                entity.getResponseContentType(),
                entity.getRequestContentLength(),
                entity.getErrorCode(),
                entity.getExceptionType(),
                entity.getRootCauseType(),
                entity.getMessage(),
                entity.getUserAgent(),
                entity.getOccurredAt()
        );
    }
}
