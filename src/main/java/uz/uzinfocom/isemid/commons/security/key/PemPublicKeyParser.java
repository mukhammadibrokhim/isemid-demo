package uz.uzinfocom.isemid.commons.security.key;

import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class PemPublicKeyParser {

    public RSAPublicKey parseRsaPublicKey(String value) {
        try {
            String normalized = normalize(value);

            byte[] decoded = Base64.getDecoder().decode(normalized);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA public key", e);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Public key value must not be blank");
        }

        String normalized = value.trim();

        if (normalized.contains("-----BEGIN PUBLIC KEY-----")) {
            normalized = normalized
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "");
        }

        return normalized
                .replace("\\n", "")
                .replaceAll("\\s", "");
    }
}