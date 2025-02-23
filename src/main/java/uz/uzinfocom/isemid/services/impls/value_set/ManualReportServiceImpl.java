package uz.uzinfocom.isemid.services.impls.value_set;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.value_set.ManualReport;
import uz.uzinfocom.isemid.models.entities.value_set.Mkb10;
import uz.uzinfocom.isemid.models.payload.requests.value_set.ReqManualReport;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.value_set.ManualReportRepository;
import uz.uzinfocom.isemid.repositories.value_set.Mkb10Repository;
import uz.uzinfocom.isemid.services.value_set.ManualReportService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.utils.CommonUtils.pageableNotSorting;

@Service
@RequiredArgsConstructor
public class ManualReportServiceImpl implements ManualReportService {
    final ManualReportRepository repo;
    final Mkb10Repository mkb10Repository;

    @Override
    public ApiResponsePage getAllByFilter(int page, int size, String shortName, String nameUz, String nameUzCyril, String nameRu, String nameKaa, String code, Boolean includeTotal, String fullURL) {
        Specification<ManualReport> spec = getFilteredSpecification(shortName, nameUz, nameUzCyril, nameRu, nameKaa, code, includeTotal);
        Page<ManualReport> mkb10Page = repo.findAll(spec, pageableNotSorting(page, size));
        return createApiResponsePage(page, mkb10Page, fullURL);
    }

    @Override
    public ApiResponse<?> create(ReqManualReport reqManualReport) {
        List<Mkb10> mkb10List = reqManualReport.getMkb10Codes().stream()
                .map(mkb10 -> mkb10Repository.findByCode(reqManualReport.getCode()))
                .toList();
        ManualReport manualReport = ManualReport.builder()
                .code(reqManualReport.getCode())
                .nameUz(reqManualReport.getNameUz())
                .nameUzCyril(reqManualReport.getNameUzCyril())
                .nameRu(reqManualReport.getNameRu())
                .nameKaa(reqManualReport.getNameKaa())
                .includeInTotal(reqManualReport.getIncludeInTotal())
                .mkb10Codes(mkb10List)
                .build();

        return new ApiResponse<>(true, "Success", repo.save(manualReport));
    }

    @Override
    public ApiResponse<?> getById(Long id) {
        ManualReport manualReport = repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Manual report not found."));
        return new ApiResponse<>(true, "Success", manualReport);
    }

    @Override
    public ApiResponse<?> delete(Long id) {
        try {
            repo.deleteById(id);
        } catch (Exception e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
        return new ApiResponse<>(true, "Success", null);
    }

    @Override
    public ApiResponse<?> put(Long id, ReqManualReport reqManualReport) {
        ManualReport manualReport = repo.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Manual report not found."));
        Optional.ofNullable(reqManualReport.getCode()).ifPresent(manualReport::setCode);
        Optional.ofNullable(reqManualReport.getShortName()).ifPresent(manualReport::setShortName);
        Optional.ofNullable(reqManualReport.getNameUz()).ifPresent(manualReport::setNameUz);
        Optional.ofNullable(reqManualReport.getNameUzCyril()).ifPresent(manualReport::setNameUzCyril);
        Optional.ofNullable(reqManualReport.getNameRu()).ifPresent(manualReport::setNameRu);
        Optional.ofNullable(reqManualReport.getNameKaa()).ifPresent(manualReport::setNameKaa);
        Optional.ofNullable(reqManualReport.getMkb10Codes()).ifPresent(reqCode -> {

        });
        return new ApiResponse<>(true, "Successfully update.", repo.save(manualReport));
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
