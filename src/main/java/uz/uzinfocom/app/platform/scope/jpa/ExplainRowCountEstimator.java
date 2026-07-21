package uz.uzinfocom.app.platform.scope.jpa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.LongSupplier;

/**
 * Parses Postgres's own {@code EXPLAIN (FORMAT JSON)} planner row estimate out of a
 * plan string, for callers that need a fast approximate row count instead of an exact
 * {@code COUNT(*)}. The planner estimate comes from table statistics (refreshed by
 * autovacuum ANALYZE) and costs only a planning pass - no rows are ever scanned - so it
 * stays near-instant regardless of table size, unlike an exact count over an
 * effectively unfiltered predicate on a large table.
 * <p>
 * Falls back to the caller-supplied exact count whenever the plan can't be parsed
 * (unexpected Postgres version/format), so a parsing gap degrades to "correct but
 * slower" rather than silently wrong.
 * <p>
 * Uses its own classic Jackson 2 {@link ObjectMapper} rather than an injected bean:
 * Spring Boot 4's default JSON auto-configuration wires a Jackson 3
 * ({@code tools.jackson.databind}) ObjectMapper, a different class entirely, and this
 * one-shot parse of a self-contained string has no need for the app's shared JSON
 * configuration (custom serializers, modules, etc.) anyway.
 */
@Slf4j
@Component
public class ExplainRowCountEstimator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public long estimate(String explainJson, LongSupplier exactCountFallback) {
        try {
            JsonNode plan = objectMapper.readTree(explainJson).path(0).path("Plan");
            long estimatedRows = plan.path("Plan Rows").asLong(-1);

            if (estimatedRows >= 0) {
                return estimatedRows;
            }
        } catch (Exception unparseablePlan) {
            log.warn("Failed to parse EXPLAIN row-count plan, falling back to exact count. reason={}",
                    unparseablePlan.getMessage());
        }

        return exactCountFallback.getAsLong();
    }
}
