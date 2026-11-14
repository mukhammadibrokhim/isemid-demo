package uz.uzinfocom.app.integration.aircontrol.client.dto;

import java.math.BigDecimal;

/** One pollutant value of a measurement (PM 2.5, CO, NO2, ...). {@code name} is as sent, not normalized. */
public record AirControlReading(String name, BigDecimal value, String unit) {
}
