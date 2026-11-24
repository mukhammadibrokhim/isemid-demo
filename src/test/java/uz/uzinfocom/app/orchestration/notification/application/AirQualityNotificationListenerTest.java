package uz.uzinfocom.app.orchestration.notification.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;
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
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirQualityNotificationListenerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SystemSettingResolver settings;

    private AirQualityNotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new AirQualityNotificationListener(userRepository, notificationRepository, settings, JsonMapper.builder().build());
    }

    private static AirQualityExceededEvent event(String regionCode, String districtCode) {
        return new AirQualityExceededEvent(
                7L, "Chilonzor", regionCode, districtCode, "PM2.5",
                new BigDecimal("31.46"), new BigDecimal("15"), new BigDecimal("2.0973"),
                LocalDateTime.of(2026, 11, 16, 9, 0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void notifiesEachSanepidUserOfTheStationTerritoryOnce() {
        when(settings.resolveBoolean(AirQualitySettingKeys.NOTIFICATION_ENABLED, true)).thenReturn(true);
        when(userRepository.findActiveIdsByMedicalTypeInTerritory(
                eq(MedicalType.SANEPID_SERVICE), eq("1726"), eq("1726266"), any()))
                .thenReturn(List.of(11L, 12L, 11L));

        listener.on(event("1726", "1726266"));

        ArgumentCaptor<Collection<Notification>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(notificationRepository).saveAll(captor.capture());
        Collection<Notification> rows = captor.getValue();
        assertThat(rows).hasSize(2);
        assertThat(rows).allSatisfy(row -> {
            assertThat(row.getType()).isEqualTo(NotificationType.AIR_QUALITY_EXCEEDED);
            assertThat(row.getEntityType()).isEqualTo(AuditEntityType.AIR_STATION);
            assertThat(row.getEntityId()).isEqualTo(7L);
            assertThat(row.getMessageKey()).isEqualTo("notification.air-quality-exceeded");
            assertThat(row.getRead()).isFalse();
            // station, pollutant, value, unit, limit - preformatted, so no locale grouping can creep in
            assertThat(row.getMessageParams()).isEqualTo("[\"Chilonzor\",\"PM2.5\",\"31.5\",\"µg/m³\",\"15.0\"]");
        });
        assertThat(rows.stream().map(Notification::getRecipientUserId).toList()).containsExactlyInAnyOrder(11L, 12L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void aStationWithoutADistrictAsksForTheWholeRegionAndKeepsTheWideLevels() {
        when(settings.resolveBoolean(AirQualitySettingKeys.NOTIFICATION_ENABLED, true)).thenReturn(true);
        when(userRepository.findActiveIdsByMedicalTypeInTerritory(any(), any(), any(), any())).thenReturn(List.of(5L));

        listener.on(event("1726", null));

        ArgumentCaptor<Collection<OrganizationLevel>> levels = ArgumentCaptor.forClass(Collection.class);
        verify(userRepository).findActiveIdsByMedicalTypeInTerritory(
                eq(MedicalType.SANEPID_SERVICE), eq("1726"), eq(null), levels.capture());
        assertThat(Set.copyOf(levels.getValue()))
                .containsExactlyInAnyOrder(OrganizationLevel.REGIONAL, OrganizationLevel.REPUBLICAN);
    }

    @Test
    void doesNothingWhenTheNotificationIsSwitchedOff() {
        when(settings.resolveBoolean(AirQualitySettingKeys.NOTIFICATION_ENABLED, true)).thenReturn(false);

        listener.on(event("1726", "1726266"));

        verifyNoInteractions(userRepository, notificationRepository);
    }

    @Test
    void aStationWithNoResolvedRegionHasNobodyToTell() {
        when(settings.resolveBoolean(eq(AirQualitySettingKeys.NOTIFICATION_ENABLED), anyBoolean())).thenReturn(true);

        listener.on(event(null, null));

        verifyNoInteractions(userRepository, notificationRepository);
    }

    @Test
    void writesNothingWhenNobodyMatches() {
        when(settings.resolveBoolean(AirQualitySettingKeys.NOTIFICATION_ENABLED, true)).thenReturn(true);
        when(userRepository.findActiveIdsByMedicalTypeInTerritory(any(), any(), any(), any())).thenReturn(List.of());

        listener.on(event("1726", "1726266"));

        verify(notificationRepository, never()).saveAll(any());
    }
}
