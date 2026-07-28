package uz.uzinfocom.app.platform.devmonitoring.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevErrorFilterRequest;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevErrorResponse;
import uz.uzinfocom.app.platform.devmonitoring.application.query.specification.DevErrorLogSpecification;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorLog;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevErrorLogRepository;
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

    private static DevErrorResponse toResponse(DevErrorLog entity) {
        return new DevErrorResponse(
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
