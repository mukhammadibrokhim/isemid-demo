package uz.uzinfocom.app.platform.resilience;

/**
 * Circuit breaker instance/config-template names for every outbound HTTP
 * integration, matched 1:1 against {@code resilience4j.circuitbreaker.*} in
 * {@code application.properties}. Kept as constants so a typo in either
 * place doesn't silently create a fresh, unconfigured breaker instead of
 * reusing the tuned one.
 *
 * <p>{@link #API2} and {@link #LIS} are single shared instance names
 * (registered directly under {@code resilience4j.circuitbreaker.instances.*}).
 * The rest are per-tenant config <em>templates</em> (registered under
 * {@code resilience4j.circuitbreaker.configs.*}) - each client builds its
 * own instance name per provider key at runtime, e.g. {@code
 * "iam-remote-" + providerKey}, via {@code
 * CircuitBreakerRegistry.circuitBreaker(instanceName, configName)}.
 */
public final class CircuitBreakerNames {

    public static final String API2 = "api2";
    public static final String LIS = "lis";
    public static final String IAM_REMOTE = "iam-remote";
    public static final String OAUTH2_LOGIN = "oauth2-login";
    public static final String RSA_PUBLIC_KEY = "rsa-public-key";
    public static final String OUTBOUND_WEBHOOK = "outbound-webhook";

    private CircuitBreakerNames() {
    }
}
