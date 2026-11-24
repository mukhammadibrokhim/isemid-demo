package uz.uzinfocom.app.modules.airquality.application.query.dto;

/**
 * One territory's (region or district) readings of one pollutant over a period; values in µg/m³.
 * {@code exceedanceDays} counts the days whose all-station average was above the norm,
 * {@code daysWithData} the days that had any reading at all.
 */
public record AirTerritoryStat(
        String territoryCode,
        long stationCount,
        long readings,
        double average,
        double maximum,
        long exceedanceDays,
        long daysWithData
) {
}
