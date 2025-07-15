package uz.uzinfocom.isemid.commons.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uz.uzinfocom.isemid.commons.dtos.ApiException;

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
}
