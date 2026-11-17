package uz.uzinfocom.app.integration.aircontrol.client.dto;

import java.time.LocalDateTime;
import java.util.List;

/** The latest measurement of one station (Method 2). {@code measuredAt} has no zone: it is the station's local wall clock. */
public record AirControlMeasurement(String stationId, LocalDateTime measuredAt, List<AirControlReading> readings) {
}
