package uz.uzinfocom.app.platform.web.response;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class PageResponseFactory {

    public <S, T> PagedResponse<T> fromPage(
            Page<S> page,
            Function<S, T> mapper,
            String message
    ) {
        List<T> data = page.getContent().stream().map(mapper).toList();
        return PagedResponse.of(
                message,
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                data
        );
    }
}
