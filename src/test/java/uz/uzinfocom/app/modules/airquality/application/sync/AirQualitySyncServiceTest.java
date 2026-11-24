package uz.uzinfocom.app.modules.airquality.application.sync;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.uzinfocom.app.integration.aircontrol.client.AirControlClient;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlMeasurement;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlReading;
import uz.uzinfocom.app.modules.airquality.application.sync.AirQualityWriter.StationRef;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirQualitySyncServiceTest {

    @Mock
    private AirControlClient airControlClient;
    @Mock
    private AirQualityWriter writer;
    @InjectMocks
    private AirQualitySyncService service;

    @Test
    void syncStationStoresTheLatestMeasurementOfThatStationOnly() {
        when(writer.findStationRef(7L)).thenReturn(Optional.of(new StationRef(7L, "21")));
        AirControlMeasurement measurement = new AirControlMeasurement("21", LocalDateTime.of(2026, 11, 23, 17, 0),
                List.of(new AirControlReading("PM 2.5", new BigDecimal("73.2"), "µg/m³")));
        when(airControlClient.fetchLatest("21")).thenReturn(Optional.of(measurement));
        when(writer.storeMeasurement(7L, measurement)).thenReturn(1);

        AirQualitySyncResult result = service.syncStation(7L);

        assertThat(result.stationsPolled()).isEqualTo(1);
        assertThat(result.measurementsStored()).isEqualTo(1);
        assertThat(result.readingsStored()).isEqualTo(1);
        assertThat(result.stationsUnchanged()).isZero();
        verify(airControlClient, never()).fetchStations();
    }

    @Test
    void syncStationCountsAnUnknownUpstreamStationAsUnchanged() {
        when(writer.findStationRef(7L)).thenReturn(Optional.of(new StationRef(7L, "21")));
        when(airControlClient.fetchLatest("21")).thenReturn(Optional.empty());

        AirQualitySyncResult result = service.syncStation(7L);

        assertThat(result.measurementsStored()).isZero();
        assertThat(result.stationsUnchanged()).isEqualTo(1);
        assertThat(result.failedStations()).isEmpty();
    }

    @Test
    void syncStationRejectsAnUnknownStationWithoutCallingUpstream() {
        when(writer.findStationRef(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.syncStation(99L)).isInstanceOf(NotFoundException.class);

        verify(airControlClient, never()).fetchLatest("99");
    }
}
