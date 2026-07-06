package uz.uzinfocom.app.platform.observability;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.observability")
public class ObservabilityProperties {

    @NotBlank
    private String traceIdHeader = "X-Trace-Id";

    private boolean acceptIncomingTraceId = true;

    @Min(1)
    @Max(64)
    private int traceIdMinLength = 16;

    @Min(16)
    @Max(128)
    private int traceIdMaxLength = 64;

    @NotBlank
    private String traceIdAllowedSeparators = "-_.:";

    @Valid
    private HttpLogging httpLogging = new HttpLogging();

    @Valid
    private OutboundHttpLogging outboundHttpLogging = new OutboundHttpLogging();

    @Valid
    private AsyncExecutor asyncExecutor = new AsyncExecutor();

    @PostConstruct
    void freezeCollections() {
        if (traceIdMinLength > traceIdMaxLength) {
            throw new IllegalStateException(
                    "app.observability.trace-id-min-length must not exceed trace-id-max-length"
            );
        }
        for (int index = 0; index < traceIdAllowedSeparators.length(); index++) {
            if ("-_.:".indexOf(traceIdAllowedSeparators.charAt(index)) < 0) {
                throw new IllegalStateException(
                        "app.observability.trace-id-allowed-separators contains an unsafe character"
                );
            }
        }
        httpLogging.setExcludedPathPrefixes(List.copyOf(httpLogging.getExcludedPathPrefixes()));
        httpLogging.setSensitiveQueryParameters(Set.copyOf(httpLogging.getSensitiveQueryParameters()));
        outboundHttpLogging.setAllowedTextContentTypes(
                List.copyOf(outboundHttpLogging.getAllowedTextContentTypes())
        );
    }

    @Getter
    @Setter
    public static class HttpLogging {

        private boolean enabled = true;
        private boolean logSuccessfulRequests = false;

        @Min(1)
        private long slowRequestThresholdMs = 1_000;

        @Min(100)
        @Max(10_000)
        private int maxTextLength = 500;

        @Min(50)
        @Max(5_000)
        private int maxUserAgentLength = 300;

        @NotBlank
        private String organizationHeader = "X-Organization-Id";

        private boolean includeQueryString = true;
        private boolean includeUserAgent = true;
        private boolean maskPathIdentifiers = true;

        private List<String> excludedPathPrefixes = new ArrayList<>(List.of(
                "/favicon.ico",
                "/actuator/health",
                "/v1/actuator/health",
                "/swagger-ui",
                "/v3/api-docs"
        ));

        private Set<String> sensitiveQueryParameters = new LinkedHashSet<>(Set.of(
                "authorization",
                "access_token",
                "refresh_token",
                "token",
                "password",
                "secret",
                "client_secret",
                "api_key",
                "apikey",
                "pinfl",
                "nnuzb",
                "ni",
                "ppn",
                "passport",
                "passport_number",
                "birth_certificate",
                "phone",
                "patient_id",
                "patient_identifier",
                "medical_record_number"
        ));
    }

    @Getter
    @Setter
    public static class OutboundHttpLogging {

        private boolean enabled = true;
        private boolean logSuccessfulRequests = false;

        @Min(1)
        private long slowRequestThresholdMs = 2_000;

        private boolean includeHeaders = false;
        private boolean logRequestBody = false;
        private boolean logResponseBody = false;

        @Min(1_024)
        @Max(1_048_576)
        private int maxBodyBytes = 16 * 1_024;

        private List<String> allowedTextContentTypes = new ArrayList<>(List.of(
                "application/json",
                "application/*+json",
                "application/xml",
                "application/*+xml",
                "application/x-www-form-urlencoded",
                "text/*"
        ));
    }

    @Getter
    @Setter
    public static class AsyncExecutor {

        @Min(1)
        private int corePoolSize = Math.max(4, Runtime.getRuntime().availableProcessors());

        @Min(1)
        private int maxPoolSize = Math.max(16, Runtime.getRuntime().availableProcessors() * 4);

        @Min(1)
        private int queueCapacity = 2_000;

        @Min(1)
        private int keepAliveSeconds = 60;

        @Min(1)
        private int awaitTerminationSeconds = 30;

        @NotBlank
        private String threadNamePrefix = "app-async-";
    }
}
