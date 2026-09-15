package uz.uzinfocom.app.orchestration.webhook.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.orchestration.webhook.application.query.dto.OutboundWebhookDispatchFilterRequest;
import uz.uzinfocom.app.orchestration.webhook.application.query.dto.OutboundWebhookDispatchResponse;
import uz.uzinfocom.app.orchestration.webhook.application.query.specification.OutboundWebhookDispatchSpecification;
import uz.uzinfocom.app.orchestration.webhook.domain.OutboundWebhookDispatch;
import uz.uzinfocom.app.orchestration.webhook.repository.OutboundWebhookDispatchRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

@Service
@RequiredArgsConstructor
public class OutboundWebhookDispatchQueryService {

    private final OutboundWebhookDispatchRepository outboundWebhookDispatchRepository;

    @Transactional(readOnly = true)
    public Page<OutboundWebhookDispatchResponse> findAll(OutboundWebhookDispatchFilterRequest request) {
        Pageable pageable = PageableUtils.of(request, OutboundWebhookDispatchSortFields.ALLOWED);

        Page<OutboundWebhookDispatch> page = outboundWebhookDispatchRepository.findAll(
                OutboundWebhookDispatchSpecification.byFilter(request),
                pageable
        );

        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OutboundWebhookDispatchResponse getById(Long id) {
        OutboundWebhookDispatch dispatch = outboundWebhookDispatchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("outbound-webhook-dispatch.not-found", id));

        return toResponse(dispatch);
    }

    private OutboundWebhookDispatchResponse toResponse(OutboundWebhookDispatch dispatch) {
        return new OutboundWebhookDispatchResponse(
                dispatch.getId(),
                dispatch.getIntegrationClientId(),
                dispatch.getEntityType(),
                dispatch.getEntityId(),
                dispatch.getOldStatus(),
                dispatch.getNewStatus(),
                dispatch.getStatus(),
                dispatch.getAttemptCount(),
                dispatch.getNextAttemptAt(),
                dispatch.getLastAttemptedAt(),
                dispatch.getLastError(),
                dispatch.getLastHttpStatus(),
                dispatch.getCreatedAt(),
                dispatch.getUpdatedAt()
        );
    }
}
