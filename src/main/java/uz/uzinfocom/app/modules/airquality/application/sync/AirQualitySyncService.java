package uz.uzinfocom.app.modules.airquality.application.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.integration.aircontrol.client.AirControlClient;
import uz.uzinfocom.app.integration.aircontrol.client.dto.AirControlMeasurement;
import uz.uzinfocom.app.integration.aircontrol.common.exception.AirControlCircuitOpenException;
import uz.uzinfocom.app.integration.aircontrol.common.exception.AirControlIntegrationException;
import uz.uzinfocom.app.modules.airquality.application.sync.AirQualityWriter.StationRef;
import uz.uzinfocom.app.modules.airquality.application.sync.AirQualityWriter.StationUpsertResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pulls stations (Method 1) and then the latest measurement of every active station
 * (Method 2) from AIRCONTROL into {@code air_station}/{@code air_measurement}. Not
 * transactional itself: every HTTP call runs outside a transaction, and
 * {@link AirQualityWriter} writes each result in its own short one, so one slow or
 * failing station never rolls back the rest.
 * <p>
 * A failure of Method 1 aborts the run (there is nothing to poll); a failure of one
 * station's Method 2 is recorded in {@code failedStations} and the run continues - except
 * an open circuit breaker, which stops polling at once instead of failing every remaining station.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AirQualitySyncService {

    private final AirControlClient airControlClient;
    private final AirQualityWriter writer;

    /** A sync takes tens of seconds; an overlapping scheduled + manual run would only duplicate upstream calls. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    public AirQualitySyncResult sync() {
        if (!running.compareAndSet(false, true)) {
            throw new AirQualitySyncInProgressException();
        }
        try {
            return doSync();
        } finally {
            running.set(false);
        }
    }

    private AirQualitySyncResult doSync() {
        var received = airControlClient.fetchStations();
        StationUpsertResult upsert = writer.upsertStations(received);

        int measurements = 0;
        int readings = 0;
        int unchanged = 0;
        List<String> failed = new ArrayList<>();
        for (StationRef station : upsert.active()) {
            try {
                Optional<AirControlMeasurement> latest = airControlClient.fetchLatest(station.externalId());
                int stored = latest.map(measurement -> writer.storeMeasurement(station.id(), measurement)).orElse(0);
                if (stored > 0) {
                    measurements++;
                    readings += stored;
                } else {
                    unchanged++;
                }
            } catch (AirControlCircuitOpenException exception) {
                log.warn("AIRCONTROL circuit is open, stopping this sync run early. stationId={}", station.externalId());
                failed.add(station.externalId());
                break;
            } catch (AirControlIntegrationException | org.springframework.dao.DataIntegrityViolationException exception) {
                // DataIntegrityViolation: another instance stored the same moment a split second earlier.
                log.warn("AIRCONTROL measurement skipped. stationId={}, reason={}",
                        station.externalId(), exception.getClass().getSimpleName());
                failed.add(station.externalId());
            }
        }

        log.info("event=air_quality_sync stations={} created={} updated={} polled={} measurements={} readings={} unchanged={} failed={}",
                received.size(), upsert.created(), upsert.updated(), upsert.active().size(),
                measurements, readings, unchanged, failed.size());
        return new AirQualitySyncResult(received.size(), upsert.created(), upsert.updated(),
                upsert.active().size(), measurements, readings, unchanged, failed);
    }
}
