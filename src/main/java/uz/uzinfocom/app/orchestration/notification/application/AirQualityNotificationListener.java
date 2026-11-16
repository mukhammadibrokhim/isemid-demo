package uz.uzinfocom.app.orchestration.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uz.uzinfocom.app.modules.airquality.application.AirQualityNorms;
import uz.uzinfocom.app.modules.airquality.application.AirQualitySettingKeys;
import uz.uzinfocom.app.modules.airquality.application.event.AirQualityExceededEvent;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.modules.iam.repository.UserRepository;
import uz.uzinfocom.app.orchestration.notification.domain.Notification;
import uz.uzinfocom.app.orchestration.notification.domain.NotificationType;
import uz.uzinfocom.app.orchestration.notification.repository.NotificationRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Turns an {@link AirQualityExceededEvent} (a station's pollutant crossed above its norm) into
 * in-app notifications for the sanitary-epidemiological service users of that territory:
 * active users of {@link MedicalType#SANEPID_SERVICE} organizations in the station's district,
 * or in its whole region when the station has no resolved district, plus the regional and
 * republican bodies of that region (they carry no district code).
 * <p>
 * Separate from {@link NotificationEventListener} because this trigger has no audit event, no
 * actor and no sender/receiver routing - it is a system observation. Toggled by
 * {@link AirQualitySettingKeys#NOTIFICATION_ENABLED}; a station with no resolved region has
 * nobody to tell and is only logged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AirQualityNotificationListener {

    static final String MESSAGE_KEY = "notification.air-quality-exceeded";

    /** Bodies that oversee a district without having a district code of their own. */
    private static final Set<OrganizationLevel> WIDE_LEVELS = Set.of(OrganizationLevel.REGIONAL, OrganizationLevel.REPUBLICAN);

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SystemSettingResolver systemSettingResolver;
    private final JsonMapper objectMapper;

    @Async("applicationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(AirQualityExceededEvent event) {
        if (!systemSettingResolver.resolveBoolean(AirQualitySettingKeys.NOTIFICATION_ENABLED, true)) {
            return;
        }
        if (!StringUtils.hasText(event.regionCode())) {
            log.warn("event=air_quality_alert_no_territory stationId={} parameter={}", event.stationId(), event.parameter());
            return;
        }

        List<Long> recipients = userRepository.findActiveIdsByMedicalTypeInTerritory(
                MedicalType.SANEPID_SERVICE, event.regionCode(),
                StringUtils.hasText(event.districtCode()) ? event.districtCode() : null, WIDE_LEVELS);
        if (recipients.isEmpty()) {
            log.info("event=air_quality_alert_no_recipients stationId={} regionCode={} districtCode={} parameter={}",
                    event.stationId(), event.regionCode(), event.districtCode(), event.parameter());
            return;
        }

        String params = writeParams(new Object[]{
                event.stationName() != null ? event.stationName() : "#" + event.stationId(),
                event.parameter(),
                format(event.value()),
                AirQualityNorms.UNIT,
                format(event.limit())
        });
        Instant occurredAt = Instant.now();
        List<Notification> rows = Set.copyOf(recipients).stream()
                .<Notification>map(recipientId -> Notification.builder()
                        .type(NotificationType.AIR_QUALITY_EXCEEDED)
                        .entityType(AuditEntityType.AIR_STATION)
                        .entityId(event.stationId())
                        .recipientUserId(recipientId)
                        .messageKey(MESSAGE_KEY)
                        .messageParams(params)
                        .read(false)
                        .occurredAt(occurredAt)
                        .build())
                .toList();

        try {
            notificationRepository.saveAll(rows);
        } catch (RuntimeException persistenceFailure) {
            log.error("event=notification_write_failure type={} entityType={} entityId={} failureType={}",
                    NotificationType.AIR_QUALITY_EXCEEDED, AuditEntityType.AIR_STATION, event.stationId(),
                    persistenceFailure.getClass().getName(), persistenceFailure);
        }
    }

    /** Pre-formatted so the message does not pick up locale grouping ("1 234,5") for a number. */
    private static String format(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private String writeParams(Object[] params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JacksonException serializationFailure) {
            log.warn("event=notification_params_serialization_failure failureType={}",
                    serializationFailure.getClass().getName());
            return null;
        }
    }
}
