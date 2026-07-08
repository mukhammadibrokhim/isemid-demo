package uz.uzinfocom.isemid.commons.configs.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "isemid.integration")
public record IntegrationProperties(

        @Valid
        Hosts hosts,

        @Valid
        Paths paths,

        @Valid
        Keys keys,

        @Valid
        Http http
) {

    public record Hosts(
            @NotBlank String ssvApiBaseUrl,
            @NotBlank String ssoBaseUrl,
            @NotBlank String lisPublicApiBaseUrl,
            @NotBlank String lisActIntegrationBaseUrl,
            @NotBlank String isemidPublicApiBaseUrl,
            @NotBlank String localApiBaseUrl
//            @NotBlank String dmedForm058ChangePushApiBaseUrl
    ) {
    }

    public record Paths(
            @NotBlank String ssoTokenPath,
            @NotBlank String ssoLogoutPath,
            @NotBlank String ssoPublicKeyPath,

            @NotBlank String practitionerPath,
            @NotBlank String organizationPath,
            @NotBlank String citizenPath,
            @NotBlank String childPath,
            @NotBlank String legalEntityPath,
            @NotBlank String vaccinationPath,

            @NotBlank String lisCreateActPath,
            @NotBlank String lisActCodePath,
            @NotBlank String lisCallbackPath
//            @NotBlank String dmedForm058ChangePushApiPath
    ) {
    }

    public record Keys(
            String lisApiKey
//            String dmedApiKey
    ) {
    }

    public record Http(
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout
    ) {
    }
}