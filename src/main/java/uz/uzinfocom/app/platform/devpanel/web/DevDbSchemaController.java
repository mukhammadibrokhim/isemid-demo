package uz.uzinfocom.app.platform.devpanel.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.devpanel.application.query.DevDbSchemaQueryService;
import uz.uzinfocom.app.platform.devpanel.application.query.DevSqlConsoleService;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevSqlQueryRequest;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevSqlQueryResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevDbRowsResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevDbSchemaResponse;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevDbTableResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

@Tag(
        name = "Dev Panel - DB schema",
        description = "Read-only view of the legacy (public) and new (public2) schemas, to follow the legacy "
                + "migration. ROLE_DEV_SUPER_ADMIN only. Row samples are off unless "
                + "app.dev.db-schema.row-preview-enabled=true."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevDbSchemaController {

    private final DevDbSchemaQueryService queryService;
    private final DevSqlConsoleService sqlConsoleService;
    private final MessageResolver messageResolver;

    @Operation(summary = "Tables of public and public2 with row counts and sizes")
    @GetMapping(ApiPaths.Dev.DB_SCHEMA)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<DevDbSchemaResponse> findSchemas(
            @Parameter(description = "true = exact count(*) per table (slower); false = planner estimate.")
            @RequestParam(defaultValue = "false") boolean exact
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), queryService.findSchemas(exact));
    }

    @Operation(summary = "Columns, constraints and indexes of one table")
    @GetMapping(ApiPaths.Dev.DB_SCHEMA_TABLE)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<DevDbTableResponse> findTable(@PathVariable String schema, @PathVariable String table) {
        return ApiResponse.success(messageResolver.resolve("common.success"), queryService.findTable(schema, table));
    }

    @Operation(summary = "Bounded row sample of one table (disabled unless the preview flag is on)")
    @GetMapping(ApiPaths.Dev.DB_SCHEMA_TABLE_ROWS)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<DevDbRowsResponse> findRows(
            @PathVariable String schema,
            @PathVariable String table,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"),
                queryService.findRows(schema, table, limit));
    }

    @Operation(summary = "Run one read-only SQL statement (disabled unless the query flag is on)")
    @PostMapping(ApiPaths.Dev.DB_SCHEMA_QUERY)
    @PreAuthorize("hasRole('DEV_SUPER_ADMIN')")
    public ApiResponse<DevSqlQueryResponse> runQuery(
            @Valid @RequestBody DevSqlQueryRequest request,
            Authentication authentication
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"),
                sqlConsoleService.execute(request.sql(), request.limit(),
                        authentication == null ? "unknown" : authentication.getName()));
    }
}
