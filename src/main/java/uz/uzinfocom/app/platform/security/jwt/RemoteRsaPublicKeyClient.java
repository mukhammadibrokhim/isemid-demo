package uz.uzinfocom.app.platform.security.jwt;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uz.uzinfocom.app.platform.resilience.CircuitBreakerNames;
import uz.uzinfocom.app.platform.resilience.DynamicCircuitBreakerLookup;

import java.security.interfaces.RSAPublicKey;

@Component
public class RemoteRsaPublicKeyClient {

    private final RestClient restClient;
    private final PemPublicKeyParser parser;
    private final DynamicCircuitBreakerLookup circuitBreakerLookup;

    public RemoteRsaPublicKeyClient(
            RestClient restClient,
            PemPublicKeyParser parser,
            DynamicCircuitBreakerLookup circuitBreakerLookup
    ) {
        this.restClient = restClient;
        this.parser = parser;
        this.circuitBreakerLookup = circuitBreakerLookup;
    }

    public RSAPublicKey fetch(String providerKey, String publicKeyUri) {
        String body;
        try {
            body = circuitBreakerLookup
                    .forProvider(CircuitBreakerNames.RSA_PUBLIC_KEY, providerKey)
                    .executeSupplier(() -> restClient.get()
                            .uri(publicKeyUri)
                            .retrieve()
                            .body(String.class));
        } catch (CallNotPermittedException exception) {
            throw new JwtException(
                    "Public key endpoint circuit breaker is open for provider " + providerKey, exception);
        }

        return parser.parse(body);
    }
}
