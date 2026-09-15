package uz.uzinfocom.app.platform.integrationclient.domain;

/**
 * How this app authenticates itself to an {@link IntegrationClient}'s own
 * webhook callback URL — distinct from {@link IntegrationAuthType}, which
 * describes the opposite direction (how the client authenticates to us).
 */
public enum OutboundWebhookAuthType {

    /** No credential sent at all. */
    NONE,

    /** {@code webhookAuthUsername} plus the decrypted secret, via HTTP Basic. */
    BASIC_AUTH,

    /** The decrypted secret sent as-is via {@code Authorization: Bearer <secret>}. */
    BEARER_TOKEN,

    /** The decrypted secret sent via a caller-named custom header. */
    API_KEY_HEADER
}
