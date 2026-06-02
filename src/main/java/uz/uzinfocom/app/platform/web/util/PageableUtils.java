package uz.uzinfocom.app.platform.web.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageableUtils {

    private final WebPaginationProperties properties;

    public Pageable of(Integer page, Integer size) {
        return of(page, size, Sort.unsorted());
    }

    public Pageable of(Integer page, Integer size, Sort sort) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        return PageRequest.of(normalizedPage - 1, normalizedSize, sort == null ? Sort.unsorted() : sort);
    }

    public int normalizePage(Integer page) {
        return page == null || page < 1 ? properties.getDefaultPage() : page;
    }

    public int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return properties.getDefaultSize();
        }
        return Math.min(size, properties.getMaxSize());
    }
}
