package uz.uzinfocom.app.integration.aircontrol.client.dto;

import java.math.BigDecimal;

/**
 * One monitoring station from Method 1. {@code id} and {@code soatoAreaId} are
 * strings because the agreement types them as {@code String} while its sample
 * shows numbers; coordinates are optional ("±" in the agreement).
 */
public record AirControlStation(
        String id,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String soatoAreaId,
        boolean active
) {
}
