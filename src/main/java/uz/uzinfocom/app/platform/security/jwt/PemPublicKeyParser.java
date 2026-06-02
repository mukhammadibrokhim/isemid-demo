package uz.uzinfocom.app.platform.security.jwt;

import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class PemPublicKeyParser {

    public RSAPublicKey parse(String pemOrBase64) {
        if (pemOrBase64 == null || pemOrBase64.isBlank()) {
            throw new IllegalArgumentException("RSA public key payload is empty");
        }

        try {
            String normalized = pemOrBase64
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(normalized);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse RSA public key", ex);
        }
    }
}
