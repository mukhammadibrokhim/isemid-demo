package uz.uzinfocom.isemid.commons.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.commons.configs.properties.IntegrationProperties;

import java.net.URI;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class IntegrationUrlFactory {

    private final IntegrationProperties properties;

    public URI ssoPublicKeyUrl() {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().ssoBaseUrl())
                .path(properties.paths().ssoPublicKeyPath())
                .build()
                .toUri();
    }

    public URI ssoTokenUrl() {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().ssoBaseUrl())
                .path(properties.paths().ssoTokenPath())
                .build()
                .toUri();
    }

    public URI ssoLogoutUrl() {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().ssoBaseUrl())
                .path(properties.paths().ssoLogoutPath())
                .build()
                .toUri();
    }

    public URI practitionerUrl(String practitionerId) {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().ssvApiBaseUrl())
                .path(properties.paths().practitionerPath())
                .encode()
                .buildAndExpand(practitionerId)
                .toUri();
    }

    public URI organizationUrl(String organizationId) {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().ssvApiBaseUrl())
                .path(properties.paths().organizationPath())
                .encode()
                .buildAndExpand(organizationId)
                .toUri();
    }

    public URI citizenUrl(String nnuzb, LocalDate birthDate, String photo) {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().ssvApiBaseUrl())
                .path(properties.paths().citizenPath())
                .queryParam("nnuzb", nnuzb)
                .queryParam("birth_date", birthDate)
                .queryParam("photo", photo)
                .encode()
                .build()
                .toUri();
    }

    public URI childUrl(String series, String number) {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().ssvApiBaseUrl())
                .path(properties.paths().childPath())
                .queryParam("series", series)
                .queryParam("number", number)
                .encode()
                .build()
                .toUri();
    }

    public URI legalEntityUrl(String tin) {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().ssvApiBaseUrl())
                .path(properties.paths().legalEntityPath())
                .queryParam("tin", tin)
                .encode()
                .build()
                .toUri();
    }

    public URI vaccinationUrl(String id) {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().ssvApiBaseUrl())
                .path(properties.paths().vaccinationPath())
                .encode()
                .buildAndExpand(id)
                .toUri();
    }

    public URI lisCreateActUrl(String actType, String senderActNumber, boolean force) {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().lisPublicApiBaseUrl())
                .path(properties.paths().lisCreateActPath())
                .queryParam("key", properties.keys().lisApiKey())
                .queryParam("senderActNumber", senderActNumber)
                .queryParam("force", force)
                .encode()
                .buildAndExpand(actType)
                .toUri();
    }

    public URI lisResearchTypeUrl(String researchCode) {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().lisPublicApiBaseUrl())
                .path(properties.paths().lisActCodePath())
                .queryParam("key", properties.keys().lisApiKey())
                .queryParam("researchCode", researchCode)
                .encode()
                .build()
                .toUri();
    }

    public URI lisCallbackUrl(Long actId) {
        return UriComponentsBuilder
                .fromUriString(properties.hosts().isemidPublicApiBaseUrl())
                .path(properties.paths().lisCallbackPath())
                .path(String.valueOf(actId))
                .build()
                .toUri();
    }

    public String lisActIntegrationBaseUrl() {
        return properties.hosts().lisActIntegrationBaseUrl();
    }
}