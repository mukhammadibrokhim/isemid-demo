package uz.uzinfocom.app.integration.aircontrol.common.properties;

/**
 * {@code system_settings} keys (editable at runtime from the dev panel) that
 * override the matching {@link AirControlProperties} value. The login and password
 * are deliberately not here: {@code system_settings} is readable by any signed-in
 * user. Timeouts are tuned through the shared {@code http-client.air-control.*} keys
 * of {@code DynamicHttpTuningInterceptor}.
 */
public final class AirControlSettingKeys {

    /** BOOLEAN, default {@code true}. {@code false} refuses calls without contacting AIR_CONTROL. */
    public static final String ENABLED = "integration.air-control.enabled";
    /** STRING, e.g. {@code https://meteoapi.meteo.uz}. */
    public static final String BASE_URL = "integration.air-control.base-url";
    /** STRING, e.g. {@code /api/atmosphere/horiba/}. */
    public static final String STATIONS_PATH = "integration.air-control.stations-path";
    /** STRING, e.g. {@code /api/atmosphere/horiba/}; the station id is appended. */
    public static final String MEASUREMENT_PATH = "integration.air-control.measurement-path";

    private AirControlSettingKeys() {
    }
}
