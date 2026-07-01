package uz.uzinfocom.app.modules.form058.infrastructure.persistence.specification;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.query.Form058Filter;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Form058Specification {

    private static final String ID = "id";
    private static final String DELETED = "deleted";
    private static final String PATIENT = "patient";

    private static final String STATUS = "status";
    private static final String SOURCE = "source";
    private static final String HAS_LINKED_CARDS = "hasLinkedCards";

    private static final String DIAGNOSIS_INFO = "diagnosisInfo";
    private static final String MKB10_CODE = "mkb10Code";

    private static final String SENDER_ORGANIZATION_ID = "senderOrganizationId";
    private static final String RECEIVER_ORGANIZATION_ID = "receiverOrganizationId";

    /**
     * Agar Organization entity ichida field nomlari stateCode/cityCode bo‘lsa,
     * shu ikkitasini regionCode/districtCode o‘rniga almashtirasan.
     */
    private static final String ORGANIZATION_REGION_CODE = "regionCode";
    private static final String ORGANIZATION_DISTRICT_CODE = "districtCode";
    private static final String ORGANIZATION_ACTIVE = "active";

    private final Form058ScopePredicateFactory form058ScopePredicateFactory;

    public Specification<Form058> table(
            Form058Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get(DELETED)));

            predicates.add(accessScopePredicate(
                    root,
                    query,
                    cb,
                    filter,
                    scope,
                    received
            ));

            applyFilters(
                    predicates,
                    root,
                    query,
                    cb,
                    filter,
                    received
            );

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public Specification<Form058> visibleById(
            Long id,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, cb) ->
                        cb.equal(root.get(ID), id)
                );
    }

    public Specification<Form058> visibleByNnuzb(
            String nnuzb,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, cb) ->
                        documentValueExists(
                                root,
                                query,
                                cb,
                                normalizeDocumentValue(nnuzb)
                        )
                );
    }

    private Specification<Form058> visible(ResolvedOrganizationScope scope) {
        return (root, query, cb) -> cb.and(
                cb.isFalse(root.get(DELETED)),
                form058ScopePredicateFactory.applyDirectionScope(
                        root,
                        query,
                        cb,
                        scope,
                        null
                )
        );
    }

    /**
     * Access scope:
     * <p>
     * Default:
     * - OUTGOING -> senderOrganizationId bo‘yicha scope
     * - INCOMING -> receiverOrganizationId bo‘yicha scope
     * - ALL      -> sender yoki receiver bo‘yicha scope
     * <p>
     * Affiliation mode:
     * - faqat patient affiliation orqali ko‘rinadi
     */
    private Predicate accessScopePredicate(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Form058Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        if (filter != null && filter.isAffiliationFilterEnabled()) {
            return patientAffiliationExists(
                    root,
                    query,
                    cb,
                    scope.organizationId()
            );
        }

        return form058ScopePredicateFactory.applyDirectionScope(
                root,
                query,
                cb,
                scope,
                received
        );
    }

    private void applyFilters(
            List<Predicate> predicates,
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Form058Filter filter,
            Boolean received
    ) {
        if (filter == null) {
            return;
        }

        if (filter.id() != null) {
            predicates.add(cb.equal(
                    root.get(ID),
                    filter.id()
            ));
        }

        if (StringUtils.hasText(filter.documentValue())) {
            predicates.add(documentValueExists(
                    root,
                    query,
                    cb,
                    normalizeDocumentValue(filter.documentValue())
            ));
        }

        if (filter.status() != null) {
            predicates.add(cb.equal(
                    root.get(STATUS),
                    filter.status()
            ));
        }

        if (StringUtils.hasText(filter.mkb10Code())) {
            predicates.add(cb.equal(
                    root.get(DIAGNOSIS_INFO).get(MKB10_CODE),
                    normalizeCode(filter.mkb10Code())
            ));
        }

        if (filter.senderOrganizationId() != null) {
            predicates.add(cb.equal(
                    root.get(SENDER_ORGANIZATION_ID),
                    filter.senderOrganizationId()
            ));
        }

        if (StringUtils.hasText(filter.regionCode()) || StringUtils.hasText(filter.districtCode())) {
            predicates.add(organizationLocationPredicate(
                    root,
                    query,
                    cb,
                    received,
                    filter.regionCode(),
                    filter.districtCode()
            ));
        }

        if (StringUtils.hasText(filter.source())) {
            predicates.add(cb.equal(
                    root.get(SOURCE),
                    normalizeCode(filter.source())
            ));
        }

        if (filter.hasLinkedCards() != null) {
            predicates.add(cb.equal(
                    root.get(HAS_LINKED_CARDS),
                    filter.hasLinkedCards()
            ));
        }
    }

    private Predicate organizationLocationPredicate(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Boolean received,
            String regionCode,
            String districtCode
    ) {
        if (received == null) {
            return cb.or(
                    root.<Long>get(SENDER_ORGANIZATION_ID).in(
                            organizationIdsByLocation(query, cb, regionCode, districtCode)
                    ),
                    root.<Long>get(RECEIVER_ORGANIZATION_ID).in(
                            organizationIdsByLocation(query, cb, regionCode, districtCode)
                    )
            );
        }

        String organizationField = Boolean.TRUE.equals(received)
                ? RECEIVER_ORGANIZATION_ID
                : SENDER_ORGANIZATION_ID;

        return root.<Long>get(organizationField).in(
                organizationIdsByLocation(query, cb, regionCode, districtCode)
        );
    }

    private Subquery<Long> organizationIdsByLocation(
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String regionCode,
            String districtCode
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Organization> organization = subquery.from(Organization.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.isTrue(organization.get(ORGANIZATION_ACTIVE)));

        if (StringUtils.hasText(regionCode)) {
            predicates.add(cb.equal(
                    organization.get(ORGANIZATION_REGION_CODE),
                    normalizeCode(regionCode)
            ));
        }

        if (StringUtils.hasText(districtCode)) {
            predicates.add(cb.equal(
                    organization.get(ORGANIZATION_DISTRICT_CODE),
                    normalizeCode(districtCode)
            ));
        }

        subquery.select(organization.get(ID))
                .where(cb.and(predicates.toArray(Predicate[]::new)));

        return subquery;
    }

    private Predicate documentValueExists(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String documentValue
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PatientIdentifier> identifier = subquery.from(PatientIdentifier.class);

        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(
                        identifier.get(PATIENT).get(ID),
                        root.get(PATIENT).get(ID)
                ),
                cb.equal(
                        identifier.get("value"),
                        documentValue
                )
        );

        return cb.exists(subquery);
    }

    private Predicate patientAffiliationExists(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Long currentOrganizationId
    ) {
        if (currentOrganizationId == null) {
            return cb.disjunction();
        }

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PatientAffiliation> affiliation = subquery.from(PatientAffiliation.class);

        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(
                        affiliation.get(PATIENT).get(ID),
                        root.get(PATIENT).get(ID)
                ),
                cb.equal(
                        affiliation.get("organizationId"),
                        currentOrganizationId
                ),
                affiliation.get("type").in(
                        AffiliationType.WORKPLACE,
                        AffiliationType.EDUCATIONAL
                )
        );

        return cb.exists(subquery);
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDocumentValue(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}