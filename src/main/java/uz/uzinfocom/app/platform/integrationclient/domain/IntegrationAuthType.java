package uz.uzinfocom.app.platform.integrationclient.domain;

/**
 * How a registered {@link IntegrationClient} proves its identity on inbound
 * requests. Exactly one applies per client — chosen at registration time,
 * immutable afterwards (revoke and re-register to switch).
 */
public enum IntegrationAuthType {

    /** {@code clientId}/{@code clientSecret} exchanged for a short-lived Bearer JWT. */
    CLIENT_CREDENTIALS,

    /** A static opaque key sent via the {@code X-Api-Key} header. */
    API_KEY,

    /** {@code clientId} as username, a dedicated secret as password, via HTTP Basic. */
    BASIC_AUTH,

    /** No credential at all — identified purely by source IP against {@code allowedIps}. */
    IP_ALLOWLIST,

    /**
     * An SSO/DHP M2M ({@code client_credentials}) Bearer token — the client is
     * matched by {@code providerKey} + {@code externalClientId} and may submit
     * on behalf of any organization of that provider.
     */
    EXTERNAL_TOKEN
}
