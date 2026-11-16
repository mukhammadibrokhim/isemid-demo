package uz.uzinfocom.app.modules.airquality.application.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlMeasurement;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlReading;
import uz.uzinfocom.app.modules.airquality.application.AirQualityNorms;
import uz.uzinfocom.app.modules.airquality.application.AirQualitySettingKeys;
import uz.uzinfocom.app.modules.airquality.application.event.AirQualityExceededEvent;
import uz.uzinfocom.app.modules.airquality.domain.AirMeasurement;
import uz.uzinfocom.app.modules.airquality.domain.AirStation;
import uz.uzinfocom.app.modules.airquality.repository.AirMeasurementRepository;
import uz.uzinfocom.app.modules.airquality.repository.AirStationRepository;
import uz.uzinfocom.app.modules.reference.repository.DistrictRepository;
import uz.uzinfocom.app.modules.reference.repository.RegionRepository;
import uz.uzinfocom.app.platform.settings.application.SystemSettingResolver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The alert is edge-triggered: only the reading that crosses above the norm raises an event. */
@ExtendWith(MockitoExtension.class)
class AirQualityWriterExceedanceTest {

    private static final Long STATION_ID = 7L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 16, 10, 0);

    @Mock
    private AirStationRepository stationRepository;
    @Mock
    private AirMeasurementRepository measurementRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private SystemSettingResolver settings;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AirQualityWriter writer;
    private AirStation station;

    @BeforeEach
    void setUp() {
        lenient().when(settings.resolveString(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(settings.resolveBoolean(eq(AirQualitySettingKeys.ALERT_ENABLED), eq(true))).thenReturn(true);
        writer = new AirQualityWriter(stationRepository, measurementRepository, regionRepository, districtRepository,
                new AirQualityNorms(settings), settings, eventPublisher);

        station = AirStation.builder().id(STATION_ID).externalId("1").name("Chilonzor")
                .regionCode("1726").districtCode("1726266")
                .lastMeasuredAt(NOW.minusHours(1)).build();
        lenient().when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(station));
    }

    private static AirControlMeasurement measurement(LocalDateTime at, String value) {
        return new AirControlMeasurement("1", at, List.of(new AirControlReading("PM 2.5", new BigDecimal(value), "µg/m³")));
    }

    private void previousReading(String value) {
        AirMeasurement previous = AirMeasurement.builder().stationId(STATION_ID).parameter("PM2.5")
                .measuredAt(NOW.minusHours(1)).value(new BigDecimal(value)).unit("µg/m³").build();
        when(measurementRepository.findFirstByStationIdAndParameterAndMeasuredAtBeforeOrderByMeasuredAtDesc(
                eq(STATION_ID), eq("PM2.5"), any())).thenReturn(Optional.of(previous));
    }

    @Test
    void raisesAnEventWhenAReadingCrossesAboveTheNorm() {
        previousReading("10");

        int stored = writer.storeMeasurement(STATION_ID, measurement(NOW, "31.5"));

        assertThat(stored).isEqualTo(1);
        ArgumentCaptor<AirQualityExceededEvent> captor = ArgumentCaptor.forClass(AirQualityExceededEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        AirQualityExceededEvent event = captor.getValue();
        assertThat(event.stationId()).isEqualTo(STATION_ID);
        assertThat(event.stationName()).isEqualTo("Chilonzor");
        assertThat(event.regionCode()).isEqualTo("1726");
        assertThat(event.districtCode()).isEqualTo("1726266");
        assertThat(event.parameter()).isEqualTo("PM2.5");
        assertThat(event.value()).isEqualByComparingTo("31.5");
        assertThat(event.limit()).isEqualByComparingTo("15");
        assertThat(event.ratio()).isEqualByComparingTo("2.1");
        assertThat(station.getLastMeasuredAt()).isEqualTo(NOW);
    }

    @Test
    void theVeryFirstReadingAboveTheNormRaisesOne() {
        when(measurementRepository.findFirstByStationIdAndParameterAndMeasuredAtBeforeOrderByMeasuredAtDesc(
                any(), any(), any())).thenReturn(Optional.empty());

        writer.storeMeasurement(STATION_ID, measurement(NOW, "20"));

        verify(eventPublisher).publishEvent(any(AirQualityExceededEvent.class));
    }

    @Test
    void aSustainedExceedanceRaisesNoSecondEvent() {
        previousReading("40");

        writer.storeMeasurement(STATION_ID, measurement(NOW, "42"));

        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void aReadingWithinTheNormRaisesNothing() {
        writer.storeMeasurement(STATION_ID, measurement(NOW, "14.9"));

        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void aLateArrivingOlderMeasurementRaisesNothing() {
        // Older than what is already stored: a backfill must not alert about the past.
        writer.storeMeasurement(STATION_ID, measurement(NOW.minusHours(5), "80"));

        verify(eventPublisher, never()).publishEvent(any(Object.class));
        assertThat(station.getLastMeasuredAt()).isEqualTo(NOW.minusHours(1));
    }

    @Test
    void doesNotAlertWhenAlertingIsSwitchedOff() {
        when(settings.resolveBoolean(AirQualitySettingKeys.ALERT_ENABLED, true)).thenReturn(false);

        writer.storeMeasurement(STATION_ID, measurement(NOW, "80"));

        verify(eventPublisher, never()).publishEvent(any(Object.class));
        verify(measurementRepository).saveAll(any());
    }

    @Test
    void anAlreadyStoredMomentIsSkippedEntirely() {
        when(measurementRepository.existsByStationIdAndMeasuredAt(STATION_ID, NOW)).thenReturn(true);

        assertThat(writer.storeMeasurement(STATION_ID, measurement(NOW, "80"))).isZero();

        verify(measurementRepository, never()).saveAll(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }
}
