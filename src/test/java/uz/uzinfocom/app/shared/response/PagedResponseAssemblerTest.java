package uz.uzinfocom.app.shared.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;
import uz.uzinfocom.app.shared.dto.response.PaginationMetadata;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagedResponseAssemblerTest {

    private static final String BASE_URI = "https://test-ykem.sanepid.uz";

    @Test
    void firstPageHasNoPrevAndKeepsPublicBaseUriAndFilters() {
        PagedResponseAssembler assembler = new PagedResponseAssembler(BASE_URI);
        MockHttpServletRequest request = request(1, 10);
        request.addParameter("active", "true");
        request.addParameter("sortBy", "createdAt");
        request.addParameter("direction", "DESC");

        PagedResponse<String> response = assembler.toResponse(
                page(0, 10, 1001, "first"),
                "ok",
                request
        );

        assertThat(response.meta().pagination())
                .isEqualTo(new PaginationMetadata(1, 10, 1, 1001, 101, true, false));
        assertThat(response.links().self())
                .isEqualTo(BASE_URI + "/api/v1/users?page=1&size=10&active=true&sortBy=createdAt&direction=DESC");
        assertThat(response.links().first())
                .isEqualTo(BASE_URI + "/api/v1/users?page=1&size=10&active=true&sortBy=createdAt&direction=DESC");
        assertThat(response.links().prev()).isNull();
        assertThat(response.links().next())
                .isEqualTo(BASE_URI + "/api/v1/users?page=2&size=10&active=true&sortBy=createdAt&direction=DESC");
        assertThat(response.links().last())
                .isEqualTo(BASE_URI + "/api/v1/users?page=101&size=10&active=true&sortBy=createdAt&direction=DESC");
    }

    @Test
    void middlePageHasPrevAndNext() {
        PagedResponseAssembler assembler = new PagedResponseAssembler(BASE_URI);

        PagedResponse<String> response = assembler.toResponse(
                page(1, 10, 1001, "middle"),
                "ok",
                request(2, 10)
        );

        assertThat(response.meta().pagination().page()).isEqualTo(2);
        assertThat(response.meta().pagination().first()).isFalse();
        assertThat(response.meta().pagination().last()).isFalse();
        assertThat(response.links().prev()).isEqualTo(BASE_URI + "/api/v1/users?page=1&size=10");
        assertThat(response.links().next()).isEqualTo(BASE_URI + "/api/v1/users?page=3&size=10");
    }

    @Test
    void lastPageHasNoNext() {
        PagedResponseAssembler assembler = new PagedResponseAssembler(BASE_URI);

        PagedResponse<String> response = assembler.toResponse(
                page(100, 10, 1001, "last"),
                "ok",
                request(101, 10)
        );

        assertThat(response.meta().pagination().page()).isEqualTo(101);
        assertThat(response.meta().pagination().last()).isTrue();
        assertThat(response.links().prev()).isEqualTo(BASE_URI + "/api/v1/users?page=100&size=10");
        assertThat(response.links().next()).isNull();
        assertThat(response.links().last()).isEqualTo(BASE_URI + "/api/v1/users?page=101&size=10");
    }

    @Test
    void singlePageHasNoPrevAndNoNext() {
        PagedResponseAssembler assembler = new PagedResponseAssembler(BASE_URI);

        PagedResponse<String> response = assembler.toResponse(
                page(0, 10, 1, "only"),
                "ok",
                request(1, 10)
        );

        assertThat(response.meta().pagination().first()).isTrue();
        assertThat(response.meta().pagination().last()).isTrue();
        assertThat(response.links().prev()).isNull();
        assertThat(response.links().next()).isNull();
    }

    @Test
    void emptyPageIsNormalizedToFirstPageAndLastLinkPointsToFirstPage() {
        PagedResponseAssembler assembler = new PagedResponseAssembler(BASE_URI + "/");
        MockHttpServletRequest request = request(3, 10);

        PagedResponse<String> response = assembler.toResponse(
                Page.empty(PageRequest.of(2, 10)),
                "ok",
                request
        );

        assertThat(response.data()).isEmpty();
        assertThat(response.meta().pagination())
                .isEqualTo(new PaginationMetadata(1, 10, 0, 0, 0, true, true));
        assertThat(response.links().self()).isEqualTo(BASE_URI + "/api/v1/users?page=1&size=10");
        assertThat(response.links().first()).isEqualTo(BASE_URI + "/api/v1/users?page=1&size=10");
        assertThat(response.links().prev()).isNull();
        assertThat(response.links().next()).isNull();
        assertThat(response.links().last()).isEqualTo(BASE_URI + "/api/v1/users?page=1&size=10");
    }

    @Test
    void queryValuesAreEncodedAndRepeatedParametersArePreserved() {
        PagedResponseAssembler assembler = new PagedResponseAssembler(BASE_URI);
        MockHttpServletRequest request = request(2, 10);
        request.addParameter("search", "San epid");
        request.addParameter("status", "ACTIVE", "PENDING");

        PagedResponse<String> response = assembler.toResponse(
                page(1, 10, 20, "value"),
                "ok",
                request
        );

        assertThat(response.links().self())
                .isEqualTo(BASE_URI + "/api/v1/users?page=2&size=10&search=San%20epid&status=ACTIVE&status=PENDING");
    }

    @Test
    void pagedResponseDefensivelyCopiesData() {
        PagedResponseAssembler assembler = new PagedResponseAssembler(BASE_URI);
        List<String> source = new java.util.ArrayList<>(List.of("first"));

        PagedResponse<String> response = assembler.toResponse(
                new PageImpl<>(source, PageRequest.of(0, 10), 1),
                "ok",
                request(1, 10)
        );

        source.add("second");

        assertThat(response.data()).containsExactly("first");
    }

    private static Page<String> page(
            int zeroBasedPage,
            int size,
            long total,
            String value
    ) {
        return new PageImpl<>(List.of(value), PageRequest.of(zeroBasedPage, size), total);
    }

    private static MockHttpServletRequest request(
            int page,
            int size
    ) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users");
        request.addParameter("page", String.valueOf(page));
        request.addParameter("size", String.valueOf(size));
        return request;
    }
}
