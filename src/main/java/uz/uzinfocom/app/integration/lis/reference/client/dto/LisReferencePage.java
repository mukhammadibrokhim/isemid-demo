package uz.uzinfocom.app.integration.lis.reference.client.dto;

import java.util.List;

/**
 * One page of a LIS paginated dictionary ({@code professions},
 * {@code research-types}, {@code categories}, {@code item-types}) —
 * {@code total} is LIS's full match count, not this page's size, so callers
 * can tell whether more pages exist.
 */
public record LisReferencePage<T>(List<T> list, Long total) {
}
