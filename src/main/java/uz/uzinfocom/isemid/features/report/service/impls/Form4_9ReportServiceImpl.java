package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.JoinType;
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
            String parentCode
    ) {
        Organization organization = requireCurrentOrganization(currentUser);

        Specification<Form058> specification = buildSpecification(
                fromDate,
                toDate,
                birthDate,
                mkb10Code,
                gender,
                organization
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
            Organization organization
    ) {
        String normalizedMkb10Code = normalizeMkb10Code(mkb10Code);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>(8);

            /*
             * Faqat Form058 entity qaytarilayotgan asosiy query uchun fetch qilinadi.
             * Kelajakda pagination ishlatilsa, count query ichida fetch join bajarilmaydi.
             */
            if (query != null && Form058.class.equals(query.getResultType())) {
                root.fetch("createdOrg", JoinType.LEFT);
                root.fetch("patient", JoinType.LEFT);
            }

            /*
             * SpecificationUtils ichida createdAt ustuniga DATE(), CAST()
             * yoki boshqa SQL function qo‘llanmasligi kerak.
             */
            SpecificationUtils.fromToDateFilter(
                    fromDate,
                    toDate,
                    root,
                    cb,
                    predicates,
                    "createdAt"
            );

            addNotApprovedPredicate(root.get("status"), cb, predicates);

            if (normalizedMkb10Code != null) {
                /*
                 * Tasdiqlanmagan formalarda initial MKB-10 kodi ishlatiladi.
                 *
                 * Ustunga UPPER/TRIM qo‘llanmayapti, chunki bu oddiy
                 * mkb10_code indeksidan foydalanishga imkon beradi.
                 */
                predicates.add(
                        cb.equal(
                                root.get("mkb10Code"),
                                normalizedMkb10Code
                        )
                );
            }

            /*
             * Alohida root.join("patient") ishlatmaymiz.
             * Aks holda patient uchun fetch join va filter join alohida-alohida
             * SQL join hosil qilishi mumkin.
             */
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
        /*
         * APPROVED bo‘lmagan barcha statuslar:
         * SENT, RECEIVED, CARD_LINKED, NOT_APPROVED, CANCELED va boshqalar.
         *
         * Eski ma’lumotlarda status NULL bo‘lishi mumkinligi hisobga olingan.
         */
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

            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED -> {
                String organizationCode = resolveOrganizationCode(organization);

                yield helper.buildOrganizations(
                        organizationCode,
                        forms
                );
            }
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

        return organization.getStateCode();
    }

    private String normalizeMkb10Code(String mkb10Code) {
        if (!hasText(mkb10Code)) {
            return null;
        }

        return mkb10Code
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