package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Start request for a database dump.")
public record DevDbDumpStartRequest(
        @Schema(description = "Schemas to dump: [\"public\"], [\"public2\"] or both. Only public and public2 are "
                + "accepted, and each must exist (see GET /db-dumps/schemas). Omit or send empty to dump "
                + "every available one.")
        List<String> schemas
) {
}
