package uz.uzinfocom.app.shared.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.Map;

public final class PageableUtils {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 200;

    private static final String DEFAULT_SORT_BY = "id";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;

    private PageableUtils() {
    }

    public static Pageable of(
            PageableRequest request,
            Map<String, String> allowedSortFields
    ) {
        return of(request, DEFAULT_SORT_BY, DEFAULT_SORT_DIRECTION, allowedSortFields);
    }

    public static Pageable pageableSortByIdDesc(int page, int size) {
        int normalizedPage = Math.max(page, 1) - 1;
        int normalizedSize = normalizeSize(size);
        return PageRequest.of(normalizedPage, normalizedSize, Sort.by(DEFAULT_SORT_DIRECTION, DEFAULT_SORT_BY));
    }

    public static Pageable of(
            PageableRequest request,
            String defaultSortBy,
            Sort.Direction defaultDirection,
            Map<String, String> allowedSortFields
    ) {
        int page = normalizePage(request.page());
        int size = normalizeSize(request.size());

        String sortBy = resolveSortBy(
                request.sortBy(),
                defaultSortBy,
                allowedSortFields
        );

        Sort.Direction direction = resolveDirection(
                request.sortDir(),
                defaultDirection
        );

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );
    }

    private static int normalizePage(Integer page) {
        int value = page == null ? DEFAULT_PAGE : page;
        return Math.max(value, 1) - 1;
    }

    private static int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    private static String resolveSortBy(
            String requestedSortBy,
            String defaultSortBy,
            Map<String, String> allowedSortFields
    ) {
        if (!StringUtils.hasText(requestedSortBy)) {
            return allowedSortFields.getOrDefault(defaultSortBy, defaultSortBy);
        }

        String normalizedSortBy = requestedSortBy.trim();

        if (!allowedSortFields.containsKey(normalizedSortBy)) {
            return allowedSortFields.getOrDefault(defaultSortBy, defaultSortBy);
        }

        return allowedSortFields.get(normalizedSortBy);
    }

    private static Sort.Direction resolveDirection(
            String requestedDirection,
            Sort.Direction defaultDirection
    ) {
        if (!StringUtils.hasText(requestedDirection)) {
            return defaultDirection;
        }

        if ("asc".equalsIgnoreCase(requestedDirection.trim())) {
            return Sort.Direction.ASC;
        }

        if ("desc".equalsIgnoreCase(requestedDirection.trim())) {
            return Sort.Direction.DESC;
        }

        return defaultDirection;
    }
}
