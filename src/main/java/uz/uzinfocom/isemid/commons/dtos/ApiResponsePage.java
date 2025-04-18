package uz.uzinfocom.isemid.commons.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponsePage {

    private boolean success;
    private String message;
    private long total;
    private List<Link> links = new ArrayList<>();
    private List<Object> data;

    public void addLink(String relation, String url) {
        this.links.add(new Link(relation, url));
    }

    @Data
    public static class Link {
        private String relation;
        private String url;

        public Link(String relation, String url) {
            this.relation = relation;
            this.url = url;
        }
    }
}
