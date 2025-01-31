package uz.uzinfocom.isemid.services.impls.value_set;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.value_set.Mkb10;
import uz.uzinfocom.isemid.models.payload.requests.ReqMkb10;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.value_set.Mkb10Repository;
import uz.uzinfocom.isemid.services.value_set.Mkb10Service;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.utils.CommonUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class Mkb10ServiceImpl implements Mkb10Service {

    final Mkb10Repository mkb10Repository;

    @Override
    public ApiResponsePage getMkb10ByFilter(int page, int size, String mkbCode, String nameUz, String nameUzCyril, String nameRu, String fullURL) {
        Specification<Mkb10> spec = getFilteredSpecification(mkbCode, nameUz, nameUzCyril, nameRu);
        Page<Mkb10> mkb10Page = mkb10Repository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, mkb10Page, fullURL);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, "Success",
                mkb10Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Mkb10 not found.")));
    }

    @Override
    public ApiResponse<?> updateById(long id, ReqMkb10 reqMkb10, User user) {
        return new ApiResponse<>(true, "Successfully update.", null);
    }

    @Override
    public ApiResponse<?> deleteById(long id) {
        try {
            mkb10Repository.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(NOT_FOUND, "Mkb10 not found.", e);
        }
        return new ApiResponse<>(true, "Successfully deleted.", null);
    }

    public static Specification<Mkb10> getFilteredSpecification(
            String mkbCode,
            String nameUz,
            String nameUzCyril,
            String nameRu
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(mkbCode)) {
                predicates.add(criteriaBuilder.equal(root.get("mkbCode"), mkbCode));
            }
            if (hasText(nameUz)) {
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUz")), "%" + nameUz + "%");
            }
            if (hasText(nameUzCyril)) {
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUzCyril")), "%" + nameUzCyril + "%");
            }
            if (hasText(nameRu)) {
                criteriaBuilder.like(criteriaBuilder.lower(root.get("nameRu")), "%" + nameRu + "%");
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
