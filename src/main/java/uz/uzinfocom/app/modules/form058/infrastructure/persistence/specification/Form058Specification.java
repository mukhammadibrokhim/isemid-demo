package uz.uzinfocom.app.modules.form058.infrastructure.persistence.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.query.Form058Filter;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Form058Specification {

    private static final int MIN_SEARCH_LENGTH = 2;
    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private Form058Specification() {
    }

    public static Specification<Form058> table(
            Form058Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
            predicates.add(directionScope(root, query, criteriaBuilder, scope, received));

            if (filter != null && filter.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
            }

            if (filter != null && filter.dateFrom() != null) {
                Instant from = filter.dateFrom()
                        .atStartOfDay(APPLICATION_ZONE)
                        .toInstant();

                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<Instant>get("createdAt"),
                        from
                ));
            }

            if (filter != null && filter.dateTo() != null) {
                Instant to = filter.dateTo()
                        .plusDays(1)
                        .atStartOfDay(APPLICATION_ZONE)
                        .toInstant();

                predicates.add(criteriaBuilder.lessThan(
                        root.<Instant>get("createdAt"),
                        to
                ));
            }

            if (filter != null && filter.hasLinkedCards() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("hasLinkedCards"),
                        filter.hasLinkedCards()
                ));
            }

            if (isSearchable(filter)) {
                predicates.add(searchPredicate(root, query, criteriaBuilder, filter.search()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Form058> visibleById(
            Long id,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("id"), id)
                );
    }

    public static Specification<Form058> visibleByNnuzb(
            String nnuzb,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, criteriaBuilder) ->
                        identifierExactExists(root, query, criteriaBuilder, "NNUZB", nnuzb)
                );
    }

    private static Specification<Form058> visible(ResolvedOrganizationScope scope) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.isFalse(root.get("deleted")),
                criteriaBuilder.or(
                        scopePredicate(root, query, criteriaBuilder, "senderOrganizationId", scope),
                        scopePredicate(root, query, criteriaBuilder, "receiverOrganizationId", scope)
                )
        );
    }

    private static Predicate directionScope(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        if (received == null) {
            return criteriaBuilder.or(
                    scopePredicate(root, query, criteriaBuilder, "senderOrganizationId", scope),
                    scopePredicate(root, query, criteriaBuilder, "receiverOrganizationId", scope)
            );
        }

        String organizationField = Boolean.TRUE.equals(received)
                ? "receiverOrganizationId"
                : "senderOrganizationId";

        return scopePredicate(root, query, criteriaBuilder, organizationField, scope);
    }

    private static Predicate scopePredicate(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            String organizationField,
            ResolvedOrganizationScope scope
    ) {
        return switch (scope.mode()) {
            case ALL -> criteriaBuilder.conjunction();

            case ORGANIZATION -> root.<Long>get(organizationField).in(
                    organizationIdsByUuid(query, criteriaBuilder, scope)
            );

            case REGION -> root.<Long>get(organizationField).in(
                    organizationIdsByRegion(query, criteriaBuilder, scope)
            );

            case DISTRICT -> root.<Long>get(organizationField).in(
                    organizationIdsByDistrict(query, criteriaBuilder, scope)
            );
        };
    }

    private static Predicate searchPredicate(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            String rawSearch
    ) {
        String search = normalizeSearch(rawSearch);

        Join<Form058, Patient> patient = root.join("patient", JoinType.LEFT);

        return criteriaBuilder.or(
                likeIgnoreCase(criteriaBuilder, patient.<String>get("firstName"), search),
                likeIgnoreCase(criteriaBuilder, patient.<String>get("lastName"), search),
                likeIgnoreCase(criteriaBuilder, patient.<String>get("middleName"), search),

                identifierValueExists(root, query, criteriaBuilder, search),

                likeIgnoreCase(criteriaBuilder, root.get("diagnosisInfo").<String>get("mkb10Code"), search),
                likeIgnoreCase(criteriaBuilder, root.get("diagnosisInfo").<String>get("mkb10Name"), search),
                likeIgnoreCase(criteriaBuilder, root.get("diagnosisInfo").<String>get("finalMkb10Code"), search),
                likeIgnoreCase(criteriaBuilder, root.get("reportInfo").<String>get("notifierFullName"), search)
        );
    }

    private static Predicate identifierValueExists(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            String search
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PatientIdentifier> identifier = subquery.from(PatientIdentifier.class);

        subquery.select(identifier.get("id"))
                .where(
                        criteriaBuilder.equal(
                                identifier.get("patient").get("id"),
                                root.get("patient").get("id")
                        ),
                        likeIgnoreCase(criteriaBuilder, identifier.<String>get("value"), search)
                );

        return criteriaBuilder.exists(subquery);
    }

    private static Predicate identifierExactExists(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            String typeCode,
            String value
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PatientIdentifier> identifier = subquery.from(PatientIdentifier.class);

        subquery.select(identifier.get("id"))
                .where(
                        criteriaBuilder.equal(
                                identifier.get("patient").get("id"),
                                root.get("patient").get("id")
                        ),
                        criteriaBuilder.equal(identifier.<String>get("typeCode"), typeCode),
                        criteriaBuilder.equal(identifier.<String>get("value"), value)
                );

        return criteriaBuilder.exists(subquery);
    }

    private static Subquery<Long> organizationIdsByUuid(
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            ResolvedOrganizationScope scope
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Organization> organization = subquery.from(Organization.class);

        subquery.select(organization.get("id"))
                .where(criteriaBuilder.equal(
                        organization.get("uuid"),
                        scope.organizationUuid()
                ));

        return subquery;
    }

    private static Subquery<Long> organizationIdsByRegion(
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            ResolvedOrganizationScope scope
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Organization> organization = subquery.from(Organization.class);

        subquery.select(organization.get("id"))
                .where(criteriaBuilder.equal(
                        organization.get("regionCode"),
                        scope.regionCode()
                ));

        return subquery;
    }

    private static Subquery<Long> organizationIdsByDistrict(
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder,
            ResolvedOrganizationScope scope
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Organization> organization = subquery.from(Organization.class);

        subquery.select(organization.get("id"))
                .where(criteriaBuilder.equal(
                        organization.get("districtCode"),
                        scope.districtCode()
                ));

        return subquery;
    }

    private static boolean isSearchable(Form058Filter filter) {
        if (filter == null || !StringUtils.hasText(filter.search())) {
            return false;
        }

        return normalizeSearch(filter.search()).length() >= MIN_SEARCH_LENGTH;
    }

    private static String normalizeSearch(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static Predicate likeIgnoreCase(
            CriteriaBuilder criteriaBuilder,
            Expression<String> field,
            String value
    ) {
        return criteriaBuilder.like(
                criteriaBuilder.lower(field),
                "%" + value + "%"
        );
    }
}