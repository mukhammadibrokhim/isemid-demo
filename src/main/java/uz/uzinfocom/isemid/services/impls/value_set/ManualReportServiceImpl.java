package uz.uzinfocom.isemid.services.impls.value_set;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.value_set.ManualReport;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.value_set.ManualReportRepository;
import uz.uzinfocom.isemid.services.value_set.ManualReportService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.utils.CommonUtils.pageableNotSorting;

@Service
@RequiredArgsConstructor
public class ManualReportServiceImpl implements ManualReportService {
    final ManualReportRepository repo;


    @Override
    public ApiResponsePage getAllByFilter(int page, int size, String shortName, String nameUz, String nameUzCyril, String nameRu, String nameKaa, String code, Boolean includeTotal, String fullURL) {
        Specification<ManualReport> spec = getFilteredSpecification(shortName, nameUz, nameUzCyril, nameRu, nameKaa, code, includeTotal);
        Page<ManualReport> mkb10Page = repo.findAll(spec, pageableNotSorting(page, size));
        return createApiResponsePage(page, mkb10Page, fullURL);
    }

    public static Specification<ManualReport> getFilteredSpecification(
            String shortName,
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameKaa,
            String code,
            Boolean includeTotal
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(shortName)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("shortName")), "%" + shortName.toLowerCase() + "%"));
            }
            if (hasText(nameUz)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUz")), "%" + nameUz.toLowerCase() + "%"));
            }
            if (hasText(nameUzCyril)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUzCyril")), "%" + nameUzCyril.toLowerCase() + "%"));
            }
            if (hasText(nameRu)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameRu")), "%" + nameRu.toLowerCase() + "%"));
            }
            if (hasText(nameKaa)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nameKaa")), "%" + nameKaa.toLowerCase() + "%"));
            }
            if (hasText(code)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
            }
            if (includeTotal != null) {
                predicates.add(criteriaBuilder.equal(root.get("includeTotal"), includeTotal));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
