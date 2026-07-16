package uz.uzinfocom.app.shared.dto.response;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;

@Component
public class PagedResponseAssembler {

    private final String baseUri;

    public PagedResponseAssembler(@Value("${app.base-uri}") String baseUri) {
        if (!StringUtils.hasText(baseUri)) {
            throw new IllegalArgumentException("app.base-uri must not be blank");
        }
        this.baseUri = trimTrailingSlash(baseUri.trim());
    }

    public <T> PagedResponse<T> toResponse(
            Page<T> page,
            String message,
            HttpServletRequest request
    ) {
        Objects.requireNonNull(page, "page must not be null");
        Objects.requireNonNull(request, "request must not be null");

        PaginationMetadata pagination = toMetadata(page);

        return new PagedResponse<>(
                true,
                message,
                page.getContent(),
                new PageMeta(pagination),
                toLinks(request, pagination)
        );
    }

    private PaginationMetadata toMetadata(Page<?> page) {
        boolean emptyResult = page.getTotalElements() == 0;

        return new PaginationMetadata(
                emptyResult ? 1 : page.getNumber() + 1,
                page.getSize(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                emptyResult || page.isFirst(),
                emptyResult || page.isLast()
        );
    }

    private PaginationLinks toLinks(
            HttpServletRequest request,
            PaginationMetadata pagination
    ) {
        int lastPage = pagination.totalPages() == 0 ? 1 : pagination.totalPages();

        String self = buildLink(request, pagination.page(), pagination.size());
        String first = buildLink(request, 1, pagination.size());
        String prev = pagination.first() ? null : buildLink(request, pagination.page() - 1, pagination.size());
        String next = pagination.last() ? null : buildLink(request, pagination.page() + 1, pagination.size());
        String last = buildLink(request, lastPage, pagination.size());

        return new PaginationLinks(self, first, prev, next, last);
    }

    private String buildLink(
            HttpServletRequest request,
            int page,
            int size
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(baseUri + normalizeRequestUri(request.getRequestURI()))
                .queryParam("page", page)
                .queryParam("size", size);

        queryParams(request).forEach((name, values) -> {
            if (!"page".equals(name) && !"size".equals(name)) {
                builder.queryParam(name, values.toArray());
            }
        });

        return builder
                .build()
                .encode()
                .toUriString();
    }

    private MultiValueMap<String, String> queryParams(HttpServletRequest request) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();

            if (name == null || values == null) {
                continue;
            }

            for (String value : values) {
                params.add(name, value);
            }
        }

        return params;
    }

    private static String normalizeRequestUri(String requestUri) {
        if (!StringUtils.hasText(requestUri)) {
            return "/";
        }

        return requestUri.startsWith("/") ? requestUri : "/" + requestUri;
    }

    private static String trimTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/") && result.length() > 1) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
