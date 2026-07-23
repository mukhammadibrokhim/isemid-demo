package uz.uzinfocom.app.integration.lis.common.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Everything needed to talk to the external LIS (Laboratory Information
 * System), mirroring {@code Api2Properties}' shape.
 *
 * <p>{@link #callbackBaseUrl} is the one outward-facing value here: it is not
 * a URL we call, it is the public base of <em>this</em> application, which we
 * hand to LIS inside the push payload so LIS knows where to post the result
 * back. It must therefore be the externally reachable address, not
 * {@code localhost}.
 */
@Validated
@ConfigurationProperties(prefix = "integration.lis")
public record LisProperties(

        @NotBlank String baseUrl,

        @NotBlank String callbackBaseUrl,

        /**
         * Static API key LIS issued to us, sent as the {@code key} query
         * parameter on every call (LIS's own scheme — not a bearer token).
         */
        @NotBlank String apiKey,

        @NotNull Duration connectTimeout,

        @NotNull Duration readTimeout,

        @Valid @NotNull Endpoints endpoints
) {

    public record Endpoints(

            /**
             * Act submission, templated on the LIS lab id — e.g.
             * {@code /create-act/{labId}}.
             */
            @NotBlank String createAct,

            /**
             * Research-type lookup that resolves a LIS act-template id from a
             * research code (WATER/FOOD/SOIL) — e.g. {@code /act-code}.
             */
            @NotBlank String actCode
    ) {
    }
}
