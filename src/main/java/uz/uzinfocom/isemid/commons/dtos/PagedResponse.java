package uz.uzinfocom.isemid.commons.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.user.models.Permission;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagedResponse<T> {

    private boolean success;
    private String message;
    private long total;
    @Builder.Default
    private List<Link> links = new ArrayList<>();
    private List<T> data;

    public static <T> PagedResponse<T> build(int page, Page<T> pageData, String requestUrl, MessageUtil messageUtil) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(requestUrl);

        String selfUrl = uriBuilder.replaceQueryParam("page", page).toUriString();
        String nextUrl = uriBuilder.replaceQueryParam("page", page + 1).toUriString();

        PagedResponse<T> response = new PagedResponse<>();
        response.setSuccess(true);
        response.setMessage(messageUtil.get("message.success"));
        response.setTotal(pageData.getTotalElements());
        response.setData(new ArrayList<>(pageData.getContent()));
        response.addLink("self", selfUrl);
        response.addLink("next", nextUrl);

        if (page > 1) {
            String prevUrl = uriBuilder.replaceQueryParam("page", page - 1).toUriString();
            response.addLink("prev", prevUrl);
        }

        return response;
    }

    public void addLink(String relation, String url) {
        this.links.add(new Link(relation, url));
    }

    @Data
    @AllArgsConstructor
    public static class Link {
        private String relation;
        private String url;
    }
}
