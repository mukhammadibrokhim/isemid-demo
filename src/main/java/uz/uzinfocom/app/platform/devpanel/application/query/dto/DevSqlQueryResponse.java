package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Result of a dev-panel SQL query; every value rendered as text, SQL NULL as JSON null.")
public record DevSqlQueryResponse(
        List<String> columns,
        List<List<String>> rows,
        @JsonProperty("isTruncated") boolean isTruncated,
        long durationMs
) {
}
