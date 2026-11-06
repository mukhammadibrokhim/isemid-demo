package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "A bounded sample of a table's rows, every value rendered as text.")
public record DevDbRowsResponse(
        String schema,
        String table,
        List<String> columns,
        List<List<String>> rows,
        int limit
) {
}
