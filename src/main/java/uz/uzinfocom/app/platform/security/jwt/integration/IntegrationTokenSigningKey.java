package uz.uzinfocom.app.platform.security.jwt.integration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * A 2048-bit RSA keypair generated once, in memory, for the lifetime of this
 * process — deliberately never persisted. Integration tokens are short-lived
 * (see {@link uz.uzinfocom.app.platform.security.jwt.properties.IntegrationTokenProperties}),
 * so a restart simply means clients re-request a token; this avoids any
 * private-key-at-rest storage/rotation concern entirely.
 */
@Slf4j
@Getter
@Component
public class IntegrationTokenSigningKey {

    private static final int KEY_SIZE_BITS = 2048;

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final String keyId;

    public IntegrationTokenSigningKey() {
        KeyPair keyPair = generateKeyPair();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        this.keyId = UUID.randomUUID().toString();

        log.info("Integration JWT signing key generated for this process lifetime. keyId={}", keyId);
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE_BITS);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException unavailableAlgorithm) {
            throw new IllegalStateException("RSA key generation is not available in this JVM", unavailableAlgorithm);
        }
    }
}
