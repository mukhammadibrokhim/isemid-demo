package uz.uzinfocom.isemid.commons.utils;

import org.springframework.data.domain.Page;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;

import java.util.ArrayList;

public class PagedResponseUtil {

    public static <T> PagedResponse<?> build(int page, Page<T> objectPage, String requestUrl,MessageUtil messageUtil) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(requestUrl);

        String selfUrl = uriBuilder.replaceQueryParam("page", page).toUriString();
        String nextUrl = uriBuilder.replaceQueryParam("page", page + 1).toUriString();

        PagedResponse<?> pagedResponse = new PagedResponse<>(
                true, messageUtil.get("message.success"),
                objectPage.getTotalElements(),
                new ArrayList<>(),
                new ArrayList<>(objectPage.getContent())
        );

        pagedResponse.addLink("self", selfUrl);
        pagedResponse.addLink("next", nextUrl);

        if (page > 1) {
            String prevUrl = uriBuilder.replaceQueryParam("page", page - 1).toUriString();
            pagedResponse.addLink("prev", prevUrl);
        }

        return pagedResponse;
    }

}
