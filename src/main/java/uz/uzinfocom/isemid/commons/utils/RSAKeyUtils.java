package uz.uzinfocom.isemid.commons.utils;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAKeyUtils {
    private static final String publicKeyValue = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxkHlR2st7aHwnMzn8lJc4f9PfsGAephgNLyTK1AzdmpJ8RmuqOGNbGYnATxFIuB51PPap76OVEOgOguIboKbAigbkFuzff/Vx+McvuXDetLJyUQizMikK+5DEXyux444f9KLvBO4FyVLSHvlki5GTHelz9REI8tYUJvn++yxDgpoPsB50O8pKeYIp240jVvc74RV6Rir/+qLm7Pdo1HVwmR7mFJV8qsMpA9MLg2FYJsSjWNPjqE4TXZB2dVRwu1jXetrfU4PFyogdO+i+SswuYTmcka9Pa1KHCfYE+NG46tmeoIFoLHeTJ3Ycj4E0G/lqCYXVyBFLxcjZcVAaK/VHwIDAQAB
            -----END PUBLIC KEY-----""";

    private static final String privateKeyValue = """
            -----BEGIN PRIVATE KEY-----
            MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDGQeVHay3tofCczOfyUlzh/09+wYB6mGA0vJMrUDN2aknxGa6o4Y1sZicBPEUi4HnU89qnvo5UQ6A6C4hugpsCKBuQW7N9/9XH4xy+5cN60snJRCLMyKQr7kMRfK7Hjjh/0ou8E7gXJUtIe+WSLkZMd6XP1EQjy1hQm+f77LEOCmg+wHnQ7ykp5ginbjSNW9zvhFXpGKv/6oubs92jUdXCZHuYUlXyqwykD0wuDYVgmxKNY0+OoThNdkHZ1VHC7WNd62t9Tg8XKiB076L5KzC5hOZyRr09rUocJ9gT40bjq2Z6ggWgsd5MndhyPgTQb+WoJhdXIEUvFyNlxUBor9UfAgMBAAECggEAD708frJ8vMWjQfd8dH7jE9KpKiF3hIQN+Icopy119cZ+D0TK2mppnVDwyMbS9ZqyNrHon8eKKhi3otQL5TtmI0IxigDVsOuQumPb14XgbSLzKmYdMyUR73uPWdc2/ZhbVj54ocTuU5wTgXIglBMrY+JdyLA2e/b75bkILjZTX2StUGGFChruXKdvSYeR3Wjh8avZ9yHP0XnnUahVTczJZB3WxIx/+QXWtaS++9Ua4GymyGOOS6E6toLS3u4vnxdZiNvfA4eiDTu1NAF3aGNcG1SDaqLt5yS+j1JSudg/PeSlw5e18mN7mioSbIUgsLhti7LZ443QGAH/vXJ6T2AdxQKBgQDrTcKga8PYeZieEqIzhGXfHSRoCK/Kfg3LdXiE9dAB/HRjIWakJpUs1uIN8A1UPo03sFGIgE29ow+iGSFX5aSBnX2zqJGX+azjf6mhGM+ARGtCUF4EyrC7CIxmTAnkZQWb3CusBw4G4BWdygMsvaF3bUn9YGnAVgkmzbAF2yQgKwKBgQDXsfpLRYhSC7mksWABrPhaLuNjI35tLMkzvBEZNum2RaCSPd0c2PYotuO3y/VAClYgF27rd0j9dE65lJ0NeZUd//a3ZJ4kuLcF6bf1soclE+7OHuP+t4+545TqFZOLLfNL36o9pb1L+xvDSsGWQRXHlhbH8gU87b+gGfqWqWgw3QKBgQCt54aUcZEJCLVW/oaKdKulvjAOA32ItqkTWfp8qf/Vt/N84E6vuyQ9Q4NGscfoH2LXaaSYsdrC8SS4tMID6GEwumkQwuD35Tt12ABSY7fYILj1Bdi2yebcFHFPmZsmBdOjYZGVTpF7W1oeiBLYmj4fudIZRYxi7jNNTm9tpIyKiQKBgBVXEoBn/LjBWDinI+yGhHvHr8+bL2L1z2JC9R4dhLn34Y8Wd83m2VPl5FnG4BjZ7PN33g2F277XxwItZexzXwF81ZgHaG4RqGn5Wa4BX8WXV0UlYUFOP/YQQid2wwY+IZgcJOe8giIGUhTcTJLlFZaNjutxUntYumDIsTY+xIQxAoGAe7D1vzCKBgx3H49lgKMfxt7qyLp2UhiQY3ueNXOFg/FatWrLftNt9yHtgKvAq6rs+Fr5VFfpjHpycTUQLYrOBKtYfXUhNUnVIRWHIUKZsN22mehZBYbVkIts/7y7HrdYjM8epwWi6tJyfPiGbkDcWSGRM/xNX7CPZ9DAdGxoqd0=
            -----END PRIVATE KEY-----""";

    private static final String SSO_SECRET = """
            -----BEGIN PUBLIC KEY-----
            MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA691ccrIMxS+akTRRSBi0
            26S6Uxr5wP/c1r/NQqaVq0EvaI1sNskP2I3QmifJUAWEsi+ACU2mO+8Ee6bGA7Pj
            OcVGVTjBiGDSEYUWyhm3F2PvFSV/PhcyE0mG38k8nQsbx/ef9Tvo6OTlko7rlKtd
            sIu6xdzJf+N9XX+k1QEh9TNqV6F2wZgOdL9dXxZxcJ9AyX05W7kc14XyL0jlMoK8
            IFWGCbFJidRvAVpbaQFnTSF/jUqkFC4pATeGcKQySNlyhUK5LK5W7mJDGmsl6ZXa
            kwiYCu5lQUe0IDTOHxuJ1yQI/deD7olaIuO8wHgJpdeXBEYZMQbzvqXn1HCTTD3W
            5YuifdNW6+ECdkgCYYfnb/4wXtKjSded5bfHpFjPejpnFTjHn18GElUp2p78XG/g
            SVfWo85O/XBxEWHmdPZGVIClGApSVAIQwGCJTfdQikJRHxjzSE55P3vAQDVT2QTU
            vFV5+Yf80fWYekoVzbd7cU1jiHZ8QWwetjqqXl3G3F83bkB0owhCaIjVLoY4Cavo
            UC546rzngYQs9kLJbNOxOm6eAvKsjDiH2eFwjGVtK2vqRl4Cud9RQbFTIR4WrD2l
            xdreJUY5xP+i2vErtX0iAJEU38vlprBNk2zqLnkuvzaluYJCm3KDfiP7SN4dyX6J
            7nwSr9iRoNQOQ4Qbx0UC+fsCAwEAAQ==
            -----END PUBLIC KEY-----""";
    private static final String TEST_SSO_SECRET = """
            -----BEGIN PUBLIC KEY-----
            MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAvi3R1S0gblU7nj1auQgp
            z+rXXpiHitiJyCdLabL+90M01Qtuno+1LlGsG6lZSAeHJ9cXH0rKZBMHM18Pip0w
            HF3tGFj6/4sV0TO70jLti120WRF4bGO7vkBwWtph8tJBBc5dDXzXvA5AFR/CbBNC
            ZjcfldDzYNOwW9+sRzewubgEzsQnjhaD0Mqh7sp3C4qPSpUsG5UgtPjTARV5IMIj
            eo7nlD9v8ztBvvbA5BcKx5h13QnExZGyoPMhOCaVE3AR7nuS0/WeKA/Mt088FOfZ
            PxtUQH59bL3leiu7VDS5QHuhN6ciepK1Hh+j9giJjhg38s3CNCQtWpOIiaQI/mUp
            aIW4tPvK5qY6bbWbTMyrmSo52/2qShizDdmQiqsJLOgFUO6HDxpoGsUsB0bbT9m2
            e/6Pe3FzZyKxHa15vcxSrIZpWMOZe3KVKVWFNXBnIF3ARfDx0ugFnnTlzXgVJBTQ
            shMPlPfsuCwjEXAROREHscMzO5cLGfnPJEpBV/ah0dxkU4KQx7sLeFvI2unr2dp9
            QVfqcZjuKVYbNvtmmTMhISuC1UDo20sTtVVKDdI+0TqYEHfPNrCpNv6Z2/kVw6ml
            IKpJyXOBovKTYx5vLdDgagteQ1Ltp4VI7NHYhjtltboASXXfldP7UAicTFVZNYTS
            6vYgu1VkHEinfZeCWtY0DY0CAwEAAQ==
            -----END PUBLIC KEY-----""";

    public static RSAPrivateKey privateKeyInternal() throws Exception {
        String privateKeyPEM = privateKeyValue
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    public static RSAPublicKey publicKeyInternal() throws Exception {
        String publicKeyPEM = publicKeyValue
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    public static RSAPublicKey publicKey() {
        try {
            String publicKeyPEM = SSO_SECRET
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert string to RSAPublicKey", e);
        }
    }

}
