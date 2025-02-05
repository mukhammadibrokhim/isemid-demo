package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.Journal060;
import uz.uzinfocom.isemid.models.entities.Patient;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.Journal060Repository;
import uz.uzinfocom.isemid.services.Journal060Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.createApiResponsePage;
import static uz.uzinfocom.isemid.utils.CommonUtils.pageableSortByIdDesc;

@Service
@RequiredArgsConstructor
public class Journal060ServiceImpl implements Journal060Service {
    final Journal060Repository journal060Repository;

    @Override
    public ApiResponsePage getAllJournal060Filter(int page, int size, Gender gender, String firstName, String lastName, String reporterFullName, String nnuzb, UUID senderOrgUuid, String receiverOrgUuid, String fullURL) {
        Specification<Journal060> spec = getFilteredSpecification(gender, firstName, lastName, reporterFullName, nnuzb, senderOrgUuid, receiverOrgUuid);
        Page<Journal060> journal060Page = journal060Repository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, journal060Page, fullURL);
    }

    @Override
    public ApiResponse<?> getById(long id) {
        return new ApiResponse<>(true, "Success",
                journal060Repository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "No record found with id: " + id)));
    }

    public static Specification<Journal060> getFilteredSpecification(
            Gender gender,
            String firstName,
            String lastName,
            String reporterFullName,
            String nnuzb,
            UUID senderOrgUuid,
            String receiverOrgUuid
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Journal060, Form058> form058Join = root.join("form058");
            Join<Form058, Patient> patientJoin = form058Join.join("patient");

            if (gender != null) {
                predicates.add(criteriaBuilder.equal(patientJoin.get("gender"), gender));
            }

            if (hasText(firstName)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(patientJoin.get("firstName")), "%" + firstName + "%"));
            }

            if (hasText(lastName)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(patientJoin.get("lastName")), "%" + lastName + "%"));
            }

            if (hasText(reporterFullName)) {
                predicates.add(criteriaBuilder.equal(form058Join.get("reporter").get("firstName"), reporterFullName));
            }
            if (hasText(nnuzb)) {
            }

            if (senderOrgUuid != null) {
            }

            if (hasText(receiverOrgUuid)) {

            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
