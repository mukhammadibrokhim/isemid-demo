package uz.uzinfocom.app.platform.devpanel.application.actuator;

import java.util.List;

/**
 * One actuator endpoint as the dev panel can reach it.
 *
 * @param id                the actuator endpoint id ({@code health}, {@code metrics}, ...)
 * @param requiredAuthority minimum dev-panel authority to read it ({@code ROLE_DEV_USER} / {@code ROLE_DEV_ADMIN})
 * @param sensitive         {@code true} for endpoints that reveal internals (env, beans, thread dumps, ...)
 * @param operations        every operation the actuator exposes for it
 */
public record DevActuatorEndpointResponse(
        String id,
        String requiredAuthority,
        boolean sensitive,
        List<Operation> operations
) {

    /**
     * @param method     {@code GET}, {@code POST} or {@code DELETE}
     * @param path       dev-panel URL template, e.g. {@code /v1/dev/actuator/metrics/{requiredMetricName}}
     * @param streamPath SSE URL template for the same operation, only for {@code GET}
     *                   (e.g. {@code /v1/dev/actuator-stream/metrics/{requiredMetricName}}), otherwise {@code null}
     * @param produces   content types the operation can produce (empty = JSON)
     */
    public record Operation(String method, String path, String streamPath, List<String> produces) {
    }
}
