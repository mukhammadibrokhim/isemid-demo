package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Tables of the two schemas the legacy migration moves between.")
public record DevDbSchemaResponse(List<SchemaInfo> schemas) {

    @Schema(description = "One schema and its tables.")
    public record SchemaInfo(
            @Schema(description = "public (legacy) or public2 (new).")
            String schema,
            List<TableInfo> tables
    ) {
    }

    public record TableInfo(
            String name,
            @Schema(description = "Planner estimate (pg_class.reltuples), -1 if never analyzed. "
                    + "Replaced by the exact count when requested with exact=true.")
            long rows,
            boolean exact,
            long totalBytes
    ) {
    }
}
