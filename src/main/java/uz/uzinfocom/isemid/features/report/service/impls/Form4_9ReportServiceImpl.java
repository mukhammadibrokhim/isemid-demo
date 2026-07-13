package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.service.Form4_9ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form4_9ReportHelper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class Form4_9ReportServiceImpl implements Form4_9ReportService {
    private final Form058Repository form058Repository;
    private final Form4_9ReportHelper helper;

    @Override
    @Transactional(readOnly = true)
    public Object getReport(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate birthDate,
            String mkb10Code,
            Gender gender,
            User currentUser,
            String parentCode,
            String stateCode,
            String cityCode
    ) {
        Organization organization = requireCurrentOrganization(currentUser);

        Specification<Form058> specification = buildSpecification(
                fromDate,
                toDate,
                birthDate,
                mkb10Code,
                gender,
                organization,
                stateCode,
                cityCode
        );

        List<Form058> forms = form058Repository.findAll(specification);

        if (hasText(parentCode)) {
            return helper.buildByParentCode(
                    parentCode.trim(),
                    forms
            );
        }

        return buildReportByOrganizationLevel(
                organization,
                forms
        );
    }

    private Specification<Form058> buildSpecification(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate birthDate,
            String mkb10Code,
            Gender gender,
            Organization organization,
            String stateCode,
            String cityCode
    ) {
        String normalizedMkb10Code = normalizeCode(mkb10Code);
        String normalizedStateCode = normalizeCode(stateCode);
        String normalizedCityCode = normalizeCode(cityCode);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>(10);

            /*
             * ManyToOne relationlarga root path orqali murojaat qilamiz.
             * Bu birthDate/gender va createdOrg filterlari uchun yetarli.
             *
             * @Transactional(readOnly = true) helper ishlayotgan vaqtda
             * persistence context ochiq qoladi.
             */
            SpecificationUtils.fromToDateFilter(
                    fromDate,
                    toDate,
                    root,
                    cb,
                    predicates,
                    "createdAt"
            );

            addNotApprovedPredicate(
                    root.get("status"),
                    cb,
                    predicates
            );

            if (normalizedMkb10Code != null) {
                predicates.add(
                        cb.equal(
                                root.get("mkb10Code"),
                                normalizedMkb10Code
                        )
                );
            }

            if (birthDate != null) {
                predicates.add(
                        cb.equal(
                                root.get("patient").get("birthDate"),
                                birthDate
                        )
                );
            }

            if (gender != null) {
                predicates.add(
                        cb.equal(
                                root.get("patient").get("gender"),
                                gender
                        )
                );
            }

            /*
             * Form058.createdOrg.stateCode bo‘yicha filter.
             */
            if (normalizedStateCode != null) {
                predicates.add(
                        cb.equal(
                                root.get("createdOrg").get("stateCode"),
                                normalizedStateCode
                        )
                );
            }

            /*
             * Form058.createdOrg.cityCode bo‘yicha filter.
             */
            if (normalizedCityCode != null) {
                predicates.add(
                        cb.equal(
                                root.get("createdOrg").get("cityCode"),
                                normalizedCityCode
                        )
                );
            }

            /*
             * Foydalanuvchi faqat o‘zining ruxsat etilgan SANEPID
             * hududi ichidagi ma’lumotlarni ko‘ra olishi kerak.
             *
             * stateCode/cityCode filterlari ushbu scope bilan AND qilinadi.
             */
            SpecificationUtils.addSanepidAccessPredicate(
                    root,
                    cb,
                    organization,
                    predicates
            );

            return cb.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }

    private void addNotApprovedPredicate(
            Path<FormStatus> statusPath,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        predicates.add(
                cb.or(
                        cb.isNull(statusPath),
                        cb.notEqual(
                                statusPath,
                                FormStatus.APPROVED
                        )
                )
        );
    }

    private Object buildReportByOrganizationLevel(
            Organization organization,
            List<Form058> forms
    ) {
        return switch (organization.getLevelType()) {
            case REPUBLICAN ->
                    helper.buildRegionReports(forms);

            case REGIONAL, URBAN ->
                    helper.buildDistrictReports(
                            organization.getStateCode(),
                            forms
                    );

            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                    helper.buildOrganizations(
                            resolveOrganizationCode(organization),
                            forms
                    );
        };
    }

    private String resolveOrganizationCode(Organization organization) {
        if (organization.getCity() != null
                && hasText(organization.getCity().getCode())) {
            return organization.getCity().getCode().trim();
        }

        if (hasText(organization.getCityCode())) {
            return organization.getCityCode().trim();
        }

        if (hasText(organization.getStateCode())) {
            return organization.getStateCode().trim();
        }

        throw new IllegalStateException(
                "Current organization stateCode or cityCode is not configured"
        );
    }

    private String normalizeCode(String code) {
        if (!hasText(code)) {
            return null;
        }

        return code
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private Organization requireCurrentOrganization(User currentUser) {
        if (currentUser == null
                || currentUser.getCurrentOrganization() == null) {
            throw new IllegalStateException(
                    "Current user organization is required for Form 4/9 report"
            );
        }

        return currentUser.getCurrentOrganization();
    }
}