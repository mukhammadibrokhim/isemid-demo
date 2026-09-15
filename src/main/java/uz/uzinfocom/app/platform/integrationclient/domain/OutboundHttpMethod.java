package uz.uzinfocom.app.platform.integrationclient.domain;

/**
 * HTTP method used to deliver a status-change notification to an
 * {@link IntegrationClient}'s own configured webhook callback URL.
 */
public enum OutboundHttpMethod {
    POST,
    PATCH
}
