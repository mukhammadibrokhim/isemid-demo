package uz.uzinfocom.app.shared.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public final class SpecificationUtils {

    private static final ZoneId TASHKENT = ZoneId.of("Asia/Tashkent");

    private SpecificationUtils() {
    }

    public static void fromToDateFilter(
            LocalDate fromDate,
            LocalDate toDate,
            Root<?> root,
            CriteriaBuilder cb,
            List<Predicate> predicates,
            String fieldName
    ) {
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get(fieldName),
                    fromDate.atStartOfDay(TASHKENT).toInstant()
            ));
        }

        if (toDate != null) {
            predicates.add(cb.lessThan(
                    root.get(fieldName),
                    toDate.plusDays(1).atStartOfDay(TASHKENT).toInstant()
            ));
        }
    }

    public static Predicate buildLevelPredicate(
            From<?, ?> source,
            CriteriaBuilder cb,
            OrganizationLevel levelType,
            String regionCode,
            String districtCode
    ) {
        return buildLevelPredicate(source, cb, levelType, regionCode, districtCode, null);
    }

    public static Predicate buildLevelPredicate(
            From<?, ?> source,
            CriteriaBuilder cb,
            OrganizationLevel levelType,
            String regionCode,
            String districtCode,
            Long organizationId
    ) {
        if (source == null || levelType == null) {
            return cb.conjunction();
        }

        Path<?> organizationPath = Organization.class.isAssignableFrom(source.getJavaType())
                ? source
                : source.get("createdOrg");

        return switch (levelType) {
            case REPUBLICAN -> cb.conjunction();
            case REGIONAL, URBAN -> regionCode == null || regionCode.isBlank()
                    ? cb.conjunction()
                    : cb.equal(organizationPath.get("regionCode"), regionCode);
            case AREA, DISTRICT, INTERDISTRICT -> {
                if (districtCode != null && !districtCode.isBlank()) {
                    yield cb.equal(organizationPath.get("districtCode"), districtCode);
                }
                if (regionCode != null && !regionCode.isBlank()) {
                    yield cb.equal(organizationPath.get("regionCode"), regionCode);
                }
                yield organizationId == null
                        ? cb.conjunction()
                        : cb.equal(organizationPath.get("id"), organizationId);
            }
            case NOT_DEFINED -> organizationId == null
                    ? cb.conjunction()
                    : cb.equal(organizationPath.get("id"), organizationId);
        };
    }
}
