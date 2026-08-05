package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.PageableUtils;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.report.dtos.request.FormReport7Request;
import uz.uzinfocom.isemid.features.report.mapper.Form7ReportMapper;
import uz.uzinfocom.isemid.features.report.model.FormReport7;
import uz.uzinfocom.isemid.features.report.repository.FormReport7Repository;
import uz.uzinfocom.isemid.features.report.service.FormReport7Service;
import uz.uzinfocom.isemid.features.report.specification.FormReport7Specification;
import uz.uzinfocom.isemid.features.report.utils.Form7StatisticsUtil;
import uz.uzinfocom.isemid.features.user.models.User;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;

@Service
@RequiredArgsConstructor
public class FormReport7ServiceImpl implements FormReport7Service {
    private final FormReport7Repository reportRepository;
    private final Form058Repository form058Repository;
    private final Form7ReportMapper form7ReportMapper;
    private final MessageUtil messageUtil;

    @Override
    public Page<FormReport7> getReport(
            int page,
            int size,
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            User currentUser
    ) {
        Specification<FormReport7> spec = FormReport7Specification.build(fromDate, toDate, stateCode, cityCode, currentUser);
        return reportRepository.findAll(spec, PageableUtils.pageableSortByIdDesc(page, size));
    }

    @Override
    public FormReport7 create(FormReport7Request request, User currentUser) {
        FormReport7 formReport7 = form7ReportMapper.toEntity(request);
        return reportRepository.save(formReport7);
    }

    @Override
    public FormReport7 getById(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", id)));
    }

    @Override
    public FormReport7 update(Long id, FormReport7Request request) {
        FormReport7 formReport7 = getById(id);
        form7ReportMapper.update(formReport7, request);
        return reportRepository.save(formReport7);
    }

    @Override
    public void delete(Long id) {
        FormReport7 report = getById(id);
        try {
            reportRepository.delete(report);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Report cannot be deleted because it is referenced by other data."
            );
        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    messageUtil.get("error.internal_server")
            );
        }
    }

    @Override
    public FormReport7Request calculate(LocalDate fromDate, LocalDate toDate, User currentUser) {
        Specification<Form058> specification = buildForm058Specification(fromDate, toDate, currentUser);
        List<Form058> form058s = form058Repository.findAll(specification);

        Integer previousEndPeriodCases = resolvePreviousEndPeriodCases(fromDate, currentUser);

        return Form7StatisticsUtil.calculate(form058s, previousEndPeriodCases);
    }

    private Integer resolvePreviousEndPeriodCases(LocalDate fromDate, User currentUser) {
        Organization organization = currentUser != null ? currentUser.getCurrentOrganization() : null;
        if (organization == null || organization.getId() == null) {
            return 0;
        }

        Timestamp periodStart = Timestamp.valueOf(fromDate.atStartOfDay());
        return reportRepository
                .findFirstByCreatedOrg_IdAndCreatedAtLessThanOrderByCreatedAtDesc(organization.getId(), periodStart)
                .map(FormReport7::getEndPeriodCases)
                .orElse(0);
    }

    private Specification<Form058> buildForm058Specification(LocalDate fromDate, LocalDate toDate, User currentUser) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");
            predicates.add(cb.notEqual(root.get("status"), FormStatus.CANCELED));

            if (currentUser != null && currentUser.getCurrentOrganization() != null && !isSuperAdmin(currentUser)) {
                Organization org = currentUser.getCurrentOrganization();
                Join<Form058, Organization> createdOrg = root.join("createdOrg", JoinType.LEFT);

                Predicate accessPredicate;
                if (org.getMedicalType() == MedicalType.SANEPID_SERVICE) {
                    accessPredicate = SpecificationUtils.buildLevelPredicate(
                            createdOrg, cb, org.getLevelType(), org.getStateCode(), org.getCityCode()
                    );
                } else {
                    accessPredicate = cb.equal(createdOrg.get("uuid"), org.getUuid());
                }
                predicates.add(accessPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}