package uz.uzinfocom.app.platform.security.jwt;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.security.interfaces.RSAPublicKey;

@Component
public class RemoteRsaPublicKeyClient {

    private final RestClient restClient;
    private final PemPublicKeyParser parser;

    public RemoteRsaPublicKeyClient(
            RestClient restClient,
            PemPublicKeyParser parser
    ) {
        this.restClient = restClient;
        this.parser = parser;
    }

    public RSAPublicKey fetch(String publicKeyUri) {
        String body = restClient.get()
                .uri(publicKeyUri)
                .retrieve()
                .body(String.class);

        return parser.parse(body);
    }
}
