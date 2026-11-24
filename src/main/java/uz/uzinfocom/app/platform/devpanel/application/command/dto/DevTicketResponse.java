package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Single-use ticket for opening an SSE stream: append '?ticket=<ticket>' to "
        + "GET /v1/dev/metrics/stream or /v1/dev/actuator-stream/{id}. Valid for a few seconds, one use.")
public record DevTicketResponse(
        String ticket,
        long expiresInSeconds
) {
}
