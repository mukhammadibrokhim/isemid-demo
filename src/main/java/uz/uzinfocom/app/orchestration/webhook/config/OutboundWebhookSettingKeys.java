package uz.uzinfocom.app.orchestration.webhook.config;

/**
 * {@code system_settings} keys (editable at runtime from the dev panel) that
 * override the matching outbound-webhook guard. Both loosen an SSRF protection,
 * so they are seeded INACTIVE - activating a row is the deliberate opt-in.
 */
public final class OutboundWebhookSettingKeys {

    /** BOOLEAN. {@code true} lets an integration client's callback URL be plain {@code http://}. */
    public static final String ALLOW_HTTP = "integration.webhook.allow-http";
    /** BOOLEAN. {@code true} lets a callback URL resolve to a private network (RFC 1918 / CGNAT / ULA). */
    public static final String ALLOW_PRIVATE_NETWORK = "integration.webhook.allow-private-network";

    private OutboundWebhookSettingKeys() {
    }
}
