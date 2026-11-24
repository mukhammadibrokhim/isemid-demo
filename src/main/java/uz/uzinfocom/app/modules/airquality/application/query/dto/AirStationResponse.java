package uz.uzinfocom.app.modules.airquality.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Air-quality monitoring station (AIR_CONTROL).")
public record AirStationResponse(
        @Schema(description = "Internal station id.", example = "1") Long id,
        @Schema(description = "AIR_CONTROL station id.", example = "1") String externalId,
        @Schema(description = "Station name.", example = "Uzhyromet") String name,
        @Schema(description = "Latitude; may be absent.") BigDecimal latitude,
        @Schema(description = "Longitude; may be absent.") BigDecimal longitude,
        @Schema(description = "SOATO code as sent by AIR_CONTROL.", example = "1726266") Integer soatoId,
        @Schema(description = "Region name in the request locale; null when the SOATO code matches nothing.") String regionName,
        @Schema(description = "District name in the request locale; null when unresolved or region-level.") String districtName,
        @Schema(description = "Whether the station is in operation.") boolean active,
        @Schema(description = "Time of the newest stored measurement (station local time).") LocalDateTime lastMeasuredAt
) {
}
