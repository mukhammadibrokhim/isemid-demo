package uz.uzinfocom.isemid.utils;

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
}
