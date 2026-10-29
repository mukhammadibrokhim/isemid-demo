package uz.uzinfocom.app.integration.dhp.common.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Server-to-server (client_credentials) access to DHP-hosted systems. Unrelated
 * to {@code app.auth.login.providers.dhp-web.*} (browser authorization_code +
 * PKCE client) and {@code app.auth.providers.dhp.*} (inbound DHP JWT
 * validation).
 *
 * <p>{@code clientId}/{@code clientSecret} are deliberately not
 * {@code @NotBlank}: prod has no defaults for them, and a missing value must
 * not stop the whole application from booting - {@code DhpAccessTokenProvider}
 * fails the DHP calls themselves with a clear "not configured" error instead.
 */
@Validated
@ConfigurationProperties(prefix = "integration.dhp")
public record DhpProperties(
        @NotBlank String tokenUrl,
        String clientId,
        String clientSecret,
        @DefaultValue("3s") @NotNull Duration connectTimeout,
        @DefaultValue("15s") @NotNull Duration readTimeout,
        /* Refresh this long before the token's real expiry so an in-flight call never carries a stale one. */
        @DefaultValue("60s") @NotNull Duration tokenExpirySkew,
        @Valid @NotNull Employment employment,
        @Valid @NotNull Fhir fhir
) {

    public record Employment(
            @NotBlank String baseUrl,
            @DefaultValue("/mol/citizen/employment/by-ni") @NotBlank String byNiEndpoint
    ) {
    }

    public record Fhir(
            @NotBlank String baseUrl,
            @DefaultValue("/Immunization") @NotBlank String immunizationEndpoint,
            /*
             * Resolves the citizen's Patient resource by NI first. Confirmed live
             * against the playground 2026-09-21: this FHIR server does not support
             * filtering Immunization by patient identifier directly - the chained
             * form (patient.identifier=) is rejected as an unsupported parameter
             * type and returns every Immunization unfiltered, and the :identifier
             * reference modifier (patient:identifier=) is accepted but never
             * matches. Only a plain reference search (patient=Patient/<id>) against
             * an id resolved via this endpoint actually filters correctly.
             */
            @DefaultValue("/Patient") @NotBlank String patientEndpoint,
            /* Optional identifier system; when set the search value becomes "system|ni". */
            String identifierSystem,
            @DefaultValue("100") @Min(1) int pageSize,
            @DefaultValue("10") @Min(1) int maxPages
    ) {
    }
}
