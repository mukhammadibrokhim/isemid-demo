package uz.uzinfocom.app.modules.airquality.application.sync;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Result of one AIRCONTROL synchronization run.")
public record AirQualitySyncResult(
        @Schema(description = "Stations received from AIRCONTROL.") int stationsReceived,
        @Schema(description = "Stations created locally.") int stationsCreated,
        @Schema(description = "Stations that already existed and were refreshed.") int stationsUpdated,
        @Schema(description = "Active stations whose latest measurement was requested.") int stationsPolled,
        @Schema(description = "Measurement moments stored (a new datetime for a station).") int measurementsStored,
        @Schema(description = "Pollutant rows stored across those measurements.") int readingsStored,
        @Schema(description = "Stations with no new measurement (unknown to AIRCONTROL or already stored).") int stationsUnchanged,
        @Schema(description = "External ids of stations whose measurement could not be fetched this run.") List<String> failedStations
) {
}
