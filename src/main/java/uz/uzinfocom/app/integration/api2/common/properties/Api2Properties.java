package uz.uzinfocom.app.integration.api2.common.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "integration.api2")
public record Api2Properties(
        @NotBlank String baseUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration readTimeout,
        @Valid @NotNull Endpoints endpoints
) {

    public record Endpoints(
            @NotBlank String child,
            @NotBlank String citizen,
            @NotBlank String citizenPassport,
            @NotBlank String legalEntity
    ) {
    }
}
