package uz.uzinfocom.app.platform.devpanel.application.actuator;

/**
 * What an actuator operation answered.
 *
 * @param status      the operation's own status (200, 204, or 503 for a {@code DOWN} health
 *                    result - the body still carries the details)
 * @param contentType the operation's declared content type, or {@code null} when it is plain JSON
 * @param body        a Jackson {@code JsonNode} for JSON results, a {@code String} for text results
 *                    (prometheus scrape, plain-text thread dump), or {@code null}
 */
public record DevActuatorResult(int status, String contentType, Object body) {

    public boolean isText() {
        return body instanceof String;
    }
}
