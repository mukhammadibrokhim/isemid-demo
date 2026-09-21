package uz.uzinfocom.app.integration.dhp.common;

import uz.uzinfocom.app.integration.dhp.common.properties.DhpProperties;

import java.time.Duration;

public final class DhpTestProperties {

    public static final String TOKEN_URL = "https://sso.dhp.example/oauth/token";
    public static final String EMPLOYMENT_BASE = "https://egov.dhp.example";
    public static final String FHIR_BASE = "https://fhir.dhp.example/fhir";

    private DhpTestProperties() {
    }

    public static DhpProperties configured() {
        return with("client-id", "client-secret", null);
    }

    public static DhpProperties with(String clientId, String clientSecret, String identifierSystem) {
        return new DhpProperties(
                TOKEN_URL,
                clientId,
                clientSecret,
                Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                Duration.ofSeconds(60),
                new DhpProperties.Employment(EMPLOYMENT_BASE, "/mol/citizen/employment/by-ni"),
                new DhpProperties.Fhir(FHIR_BASE, "/Immunization", "patient.identifier", identifierSystem, 100, 3)
        );
    }
}
