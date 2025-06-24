package uz.uzinfocom.isemid.commons.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;

import java.util.ArrayList;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@UtilityClass
public class PageableUtils {

    private static void validatePageAndSize(int page, int size) {
        if (page <= 0) {
            throw new ApiException(BAD_REQUEST, "Номер страницы не может быть меньше 0.");
        }
        if (size <= 0 || size > 100) {
            throw new ApiException(BAD_REQUEST, "Размер должен быть больше 0 и меньше или равен 100.");
        }
    }

    public static Pageable pageableSortByIdDesc(int page, int size) {
        validatePageAndSize(page, size);
        return PageRequest.of(page - 1, size, Sort.by("id").descending());
    }

    public static Pageable pageableNotSorting(int page, int size) {
        validatePageAndSize(page, size);
        return PageRequest.of(page - 1, size);
    }

    public static <T> PagedResponse<?> createApiResponsePage(int page, Page<T> objectPage, String requestUrl) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(requestUrl);

        String selfUrl = uriBuilder.replaceQueryParam("page", page).toUriString();
        String nextUrl = uriBuilder.replaceQueryParam("page", page + 1).toUriString();

        PagedResponse<?> pagedResponse = new PagedResponse<>(
                true, MessageUtil.OPERATION_SUCCESS,
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
