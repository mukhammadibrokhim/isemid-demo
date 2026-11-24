package uz.uzinfocom.app.integration.lis.reference.client.dto;

/**
 * The body LIS expects on its four paginated dictionary lookups
 * ({@code professions}, {@code research-types}, {@code categories},
 * {@code item-types}) — LIS's own {@code Pagination<Search>} shape,
 * verified against its live test responses rather than assumed. Field names
 * are exactly {@code search}/{@code page}/{@code limit}; anything else
 * (e.g. {@code size}, {@code pageSize}, {@code pageNumber}) is rejected by
 * LIS with a 400.
 *
 * <p>{@code search.value} must not be {@code null} — LIS's
 * {@code research-types}/{@code item-types}/{@code categories} endpoints NPE
 * server-side (400) if it is, even though an empty string is accepted and
 * matches everything.
 */
public record LisPaginationRequest(LisSearch search, Integer page, Integer limit) {

    public static LisPaginationRequest of(String searchValue, int page, int limit) {
        return new LisPaginationRequest(new LisSearch(searchValue == null ? "" : searchValue), page, limit);
    }

    public record LisSearch(String value) {
    }
}
