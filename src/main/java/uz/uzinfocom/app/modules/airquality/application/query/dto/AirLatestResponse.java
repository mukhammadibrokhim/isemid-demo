package uz.uzinfocom.app.modules.airquality.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Newest stored measurement of a station with all its pollutants.")
public record AirLatestResponse(
        AirStationResponse station,
        @Schema(description = "Measurement time; null when no measurement is stored yet.") LocalDateTime measuredAt,
        List<AirReadingResponse> readings
) {
}
