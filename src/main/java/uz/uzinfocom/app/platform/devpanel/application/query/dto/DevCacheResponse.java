package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One application cache with its live size and hit/miss statistics.")
public record DevCacheResponse(
        @Schema(description = "Cache name (use it in DELETE /v1/dev/caches/{name}).")
        String name,
        @Schema(description = "Estimated number of entries currently held.")
        long size,
        long hitCount,
        long missCount,
        @Schema(description = "hitCount / (hitCount + missCount), 0 when the cache was never read.")
        double hitRate,
        long evictionCount,
        @Schema(description = "True when the cache is never cleared from the dev panel (e.g. the revoked-token blacklist).")
        boolean protectedFromClear
) {
}
