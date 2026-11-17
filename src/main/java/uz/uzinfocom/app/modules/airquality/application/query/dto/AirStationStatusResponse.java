package uz.uzinfocom.app.modules.airquality.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.airquality.application.AirStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "A station with its latest measurement judged against the pollutant norms - what a map pin or a "
        + "dashboard row needs.")
public record AirStationStatusResponse(
        AirStationResponse station,
        @Schema(description = "Moment of the latest stored measurement (station local time); null until one arrives.")
        LocalDateTime measuredAt,
        @Schema(description = "The worst status among the pollutants that have a norm; NO_DATA when none could be judged.")
        AirStatus status,
        @Schema(description = "The pollutant behind {@code status}; null when NO_DATA.", example = "PM2.5")
        String worstParameter,
        @Schema(description = "Its value / limit ratio; null when NO_DATA.", example = "2.1000")
        BigDecimal worstRatio,
        List<AirReadingStatusResponse> readings
) {
}
