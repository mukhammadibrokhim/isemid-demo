package uz.uzinfocom.app.platform.devpanel.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Start request for a database dump.")
public record DevDbDumpStartRequest(
        @Schema(description = "Schemas to dump (e.g. [\"public\",\"public2\"]). Defaults to every schema "
                + "of the connected database that the migration touches: public and public2.")
        List<String> schemas
) {
}
