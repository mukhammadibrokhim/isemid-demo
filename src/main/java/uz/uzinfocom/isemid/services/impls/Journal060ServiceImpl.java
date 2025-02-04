package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.Journal060;
import uz.uzinfocom.isemid.models.payload.requests.ReqJournal060;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.Journal060Repository;
import uz.uzinfocom.isemid.services.Journal060Service;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.utils.CommonUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.utils.CommonUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class Journal060ServiceImpl implements Journal060Service {
    final Journal060Repository journal060Repository;

    @Override
    public ApiResponsePage getAllJournal060Filter(int page, int size, String url) {
        Specification<Journal060> spec = getFilteredSpecification();
        Page<Journal060> journal060Page = journal060Repository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, journal060Page, url);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, "Success",
                journal060Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "No record found with id: " + id)));
    }

    @Override
    public ApiResponse<?> update(long id, ReqJournal060 reqJournal060) {
        return new ApiResponse<>(true, "success", null);
    }


    public static Specification<Journal060> getFilteredSpecification(
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
