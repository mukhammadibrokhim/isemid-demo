package uz.uzinfocom.app.integration.muxlisa.common.properties;

/**
 * {@code system_settings} keys (editable at runtime from the dev panel) that
 * override the matching {@link MuxlisaProperties} value. The API key and the
 * concurrency cap are deliberately not here: the key is a secret and
 * {@code system_settings} is readable by any signed-in user, and the cap sizes
 * a {@code Semaphore} created once at startup. Timeouts are tuned through the
 * shared {@code http-client.muxlisa.*} keys of {@code DynamicHttpTuningInterceptor}.
 */
public final class MuxlisaSettingKeys {

    /** BOOLEAN, default {@code true}. {@code false} refuses STT calls without contacting Muxlisa. */
    public static final String ENABLED = "integration.muxlisa.enabled";
    /** STRING, e.g. {@code https://service.muxlisa.uz}. */
    public static final String BASE_URL = "integration.muxlisa.base-url";
    /** STRING, e.g. {@code /api/v2/stt}. */
    public static final String STT_ENDPOINT = "integration.muxlisa.stt-endpoint";
    /** NUMBER (bytes), e.g. {@code 5242880}. */
    public static final String MAX_AUDIO_SIZE_BYTES = "integration.muxlisa.max-audio-size-bytes";

    private MuxlisaSettingKeys() {
    }
}
