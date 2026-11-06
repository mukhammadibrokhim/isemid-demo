package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Structure of one table: columns, constraints and indexes.")
public record DevDbTableResponse(
        String schema,
        String table,
        List<Column> columns,
        List<Constraint> constraints,
        List<Index> indexes
) {

    public record Column(
            String name,
            String type,
            boolean nullable,
            Integer maxLength,
            String defaultValue
    ) {
    }

    public record Constraint(
            String name,
            @Schema(description = "PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK or EXCLUDE.")
            String type,
            @Schema(description = "pg_get_constraintdef output.")
            String definition
    ) {
    }

    public record Index(String name, String definition) {
    }
}
