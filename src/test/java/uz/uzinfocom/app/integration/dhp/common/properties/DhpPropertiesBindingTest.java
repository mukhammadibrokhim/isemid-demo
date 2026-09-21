package uz.uzinfocom.app.integration.dhp.common.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the property contract the dev/prod profiles rely on: nested records
 * bind, defaults apply, and - the prod case - an empty client id/secret must
 * NOT stop the application from booting.
 */
class DhpPropertiesBindingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(Config.class);

    @Test
    void bindsRequiredValuesAndAppliesDefaults() {
        runner.withPropertyValues(
                "integration.dhp.token-url=https://sso.example/oauth/token",
                "integration.dhp.client-id=id",
                "integration.dhp.client-secret=secret",
                "integration.dhp.employment.base-url=https://egov.example",
                "integration.dhp.fhir.base-url=https://fhir.example/fhir"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            DhpProperties properties = context.getBean(DhpProperties.class);

            assertThat(properties.clientId()).isEqualTo("id");
            assertThat(properties.connectTimeout()).isEqualTo(Duration.ofSeconds(3));
            assertThat(properties.readTimeout()).isEqualTo(Duration.ofSeconds(15));
            assertThat(properties.tokenExpirySkew()).isEqualTo(Duration.ofSeconds(60));
            assertThat(properties.employment().byNiEndpoint()).isEqualTo("/mol/citizen/employment/by-ni");
            assertThat(properties.fhir().immunizationEndpoint()).isEqualTo("/Immunization");
            assertThat(properties.fhir().patientSearchParam()).isEqualTo("patient.identifier");
            assertThat(properties.fhir().pageSize()).isEqualTo(100);
            assertThat(properties.fhir().maxPages()).isEqualTo(10);
        });
    }

    @Test
    void emptyCredentialsStillBindSoProdCanBootWithoutThem() {
        runner.withPropertyValues(
                "integration.dhp.token-url=https://sso.example/oauth/token",
                "integration.dhp.client-id=",
                "integration.dhp.client-secret=",
                "integration.dhp.employment.base-url=https://egov.example",
                "integration.dhp.fhir.base-url=https://fhir.example/fhir",
                "integration.dhp.fhir.identifier-system="
        ).run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void aMissingBaseUrlFailsFast() {
        runner.withPropertyValues(
                "integration.dhp.token-url=https://sso.example/oauth/token",
                "integration.dhp.employment.base-url=https://egov.example"
        ).run(context -> assertThat(context).hasFailed());
    }

    @Configuration
    @EnableConfigurationProperties(DhpProperties.class)
    static class Config {
    }
}
