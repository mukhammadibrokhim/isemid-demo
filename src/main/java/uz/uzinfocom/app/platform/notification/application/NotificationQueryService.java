package uz.uzinfocom.app.platform.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.notification.application.dto.NotificationFilterRequest;
import uz.uzinfocom.app.platform.notification.application.dto.NotificationResponse;
import uz.uzinfocom.app.platform.notification.domain.Notification;
import uz.uzinfocom.app.platform.notification.repository.NotificationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final MessageResolver messageResolver;
    private final JsonMapper objectMapper;

    public Page<NotificationResponse> findMine(Long currentUserId, NotificationFilterRequest filter) {
        Pageable pageable = PageRequest.of(
                normalizePage(filter.page()),
                normalizeSize(filter.size()),
                Sort.by(Sort.Direction.DESC, "occurredAt")
        );

        Page<Notification> page = Boolean.TRUE.equals(filter.unreadOnly())
                ? notificationRepository.findByRecipientUserIdAndReadFalseOrderByOccurredAtDesc(currentUserId, pageable)
                : notificationRepository.findByRecipientUserIdOrderByOccurredAtDesc(currentUserId, pageable);

        return page.map(this::toResponse);
    }

    public long countUnread(Long currentUserId) {
        return notificationRepository.countByRecipientUserIdAndReadFalse(currentUserId);
    }

    NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getEntityType(),
                notification.getEntityId(),
                notification.getOrganizationId(),
                resolveMessage(notification),
                Boolean.TRUE.equals(notification.getRead()),
                notification.getReadAt(),
                notification.getOccurredAt()
        );
    }

    private String resolveMessage(Notification notification) {
        return messageResolver.resolve(notification.getMessageKey(), parseParams(notification.getMessageParams()));
    }

    private Object[] parseParams(String messageParams) {
        if (messageParams == null || messageParams.isBlank()) {
            return new Object[0];
        }
        try {
            return objectMapper.readValue(messageParams, Object[].class);
        } catch (Exception parseFailure) {
            log.warn("event=notification_params_parse_failure failureType={}", parseFailure.getClass().getName());
            return new Object[0];
        }
    }

    private int normalizePage(Integer page) {
        int value = page == null ? 1 : page;
        return Math.max(value, 1) - 1;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 200);
    }
}
