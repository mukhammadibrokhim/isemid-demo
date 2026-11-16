package uz.uzinfocom.app.modules.airquality.application;

/**
 * Runtime-editable {@code system_settings} keys of the air-quality module (seeded by
 * {@code 20261116-1100-seed-air-quality-norm-settings.xml} so they show up in the dev
 * panel; an unseeded key falls back to the code default).
 */
public final class AirQualitySettingKeys {

    /** Per-pollutant limit in micrograms per cubic metre; the key is this prefix + the lower-cased code, e.g. {@code air-quality.norm.pm2.5}. */
    public static final String NORM_PREFIX = "air-quality.norm.";

    /** BOOLEAN, default {@code true}. {@code false} stops threshold alerts from being raised at all. */
    public static final String ALERT_ENABLED = "air-quality.alert.enabled";

    /** BOOLEAN, default {@code true}. Notification kind toggle, same family as {@code notification.*.enabled}. */
    public static final String NOTIFICATION_ENABLED = "notification.air-quality-exceeded.enabled";

    private AirQualitySettingKeys() {
    }
}
