package uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129DailyCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129DiseaseType;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129DiseaseTypeCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129MonthlyOutcomeCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129OrganizationCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129SourceCountResponse;
import uz.uzinfocom.app.modules.form129.application.stats.query.dto.Form129StatusCountResponse;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.OrganizationScopePredicateFactory;
import uz.uzinfocom.app.platform.stats.AbstractCaseStatsRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Aggregation queries for the form129 dashboard tab — built on
 * {@link AbstractCaseStatsRepository} like {@code Form0581StatsRepository},
 * but scoped by {@code receiverOrganizationId} only (Form129 has no direction
 * toggle at this layer — see {@code Form129StatsQueryService}).
 */
@Repository
public class Form129StatsRepository extends AbstractCaseStatsRepository<Form129> {

    private static final String RECEIVER_ORGANIZATION_ID = "receiverOrganizationId";

    private final OrganizationScopePredicateFactory scopePredicateFactory;

    public Form129StatsRepository(EntityManager entityManager, OrganizationScopePredicateFactory scopePredicateFactory) {
        super(entityManager, Form129.class);
        this.scopePredicateFactory = scopePredicateFactory;
    }

    /** Total case count in scope — a direct {@code COUNT(*)}, not a sum over {@link #countByStatus}. */
    public long countTotal(ResolvedOrganizationScope scope) {
        return countAll((root, cb) -> receiverScope(root, cb, scope));
    }

    /** Count of forms still awaiting the receiver's accept/reject decision ({@code status = SENT}). */
    public long countActive(ResolvedOrganizationScope scope) {
        return countAll((root, cb) -> cb.and(
                receiverScope(root, cb, scope),
                cb.equal(root.get("status"), Form129Status.SENT)
        ));
    }

    public List<Form129StatusCountResponse> countByStatus(ResolvedOrganizationScope scope) {
        return countGrouped(
                (root, cb) -> root.<Form129Status>get("status"),
                (root, cb) -> receiverScope(root, cb, scope),
                Form129StatusCountResponse::new
        );
    }

    /** Grouped by source (e.g. MANUAL/SSO/DHP) — for the home dashboard's source breakdown. */
    public List<Form129SourceCountResponse> countBySource(ResolvedOrganizationScope scope) {
        return countGrouped(
                (root, cb) -> root.<String>get("source"),
                (root, cb) -> receiverScope(root, cb, scope),
                Form129SourceCountResponse::new
        );
    }

    /**
     * Grouped by disease type — the form129 dashboard's counterpart to
     * Form0581's top-ICD10 breakdown. See {@link Form129DiseaseType}.
     */
    public List<Form129DiseaseTypeCountResponse> countByDiseaseType(ResolvedOrganizationScope scope) {
        return countGrouped(
                this::diseaseTypeOrdinal,
                (root, cb) -> receiverScope(root, cb, scope),
                (Integer ordinal, Long count) ->
                        new Form129DiseaseTypeCountResponse(Form129DiseaseType.values()[ordinal], count)
        );
    }

    public List<Form129DailyCountResponse> countByDay(ResolvedOrganizationScope scope, LocalDate fromDate, LocalDate toDate) {
        return countByDateBucket(
                "day",
                (root, cb) -> receiverScope(root, cb, scope),
                fromDate, toDate,
                Form129DailyCountResponse::new
        );
    }

    /**
     * Same as {@link #countByDay}, plus a CANCELED/ACCEPTED breakdown per
     * month — for the home dashboard's dynamics chart.
     */
    public List<Form129MonthlyOutcomeCountResponse> countByMonthWithOutcomes(
            ResolvedOrganizationScope scope,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return countByDateBucketWithOutcomes(
                "month",
                (root, cb) -> receiverScope(root, cb, scope),
                fromDate, toDate,
                (root, cb) -> cb.equal(root.get("status"), Form129Status.CANCELED),
                (root, cb) -> cb.equal(root.get("status"), Form129Status.ACCEPTED),
                Form129MonthlyOutcomeCountResponse::new
        );
    }

    /**
     * Restricted-id variant, safe for non-admin callers because the caller
     * supplies the exact organization ids to aggregate (already resolved
     * from a legitimate scope elsewhere, e.g. the home dashboard's
     * region/district breakdown) — mirrors
     * {@code Form0581StatsRepository#countByReceiverOrganizationWithinIds}.
     */
    public List<Form129OrganizationCountResponse> countByReceiverOrganizationWithinIds(List<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        return countGrouped(
                (root, cb) -> root.<Long>get(RECEIVER_ORGANIZATION_ID),
                (root, cb) -> root.<Long>get(RECEIVER_ORGANIZATION_ID).in(organizationIds),
                Form129OrganizationCountResponse::new
        );
    }

    /**
     * Form129 has no {@code deleteInfo} concept at this layer's usual filter
     * point beyond what {@link #notDeleted} already applies via reflection on
     * {@code deleteInfo.deleted} — inherited unchanged from the base class.
     */
    private Predicate receiverScope(Root<Form129> root, CriteriaBuilder cb, ResolvedOrganizationScope scope) {
        return scopePredicateFactory.apply(root, cb, RECEIVER_ORGANIZATION_ID, scope);
    }

    /**
     * Returns the disease type's ordinal rather than the enum itself: Hibernate
     * encodes {@code selectCase()} literals for an unmapped Java enum (no
     * {@code @Enumerated} attribute/converter backs {@link Form129DiseaseType})
     * as plain integers and hands back a raw {@code Integer} at read time, so
     * asking the criteria API for {@code Expression<Form129DiseaseType>} here
     * only sets up a runtime {@code ClassCastException} once the tuple is
     * mapped back to the enum — translate the ordinal explicitly instead.
     */
    private Expression<Integer> diseaseTypeOrdinal(Root<Form129> root, CriteriaBuilder cb) {
        var labResults = root.get("labResults");

        return cb.<Integer>selectCase()
                .when(cb.isNotNull(labResults.get("wrightHeddelsonOutcome")), Form129DiseaseType.BRUCELLOSIS.ordinal())
                .when(cb.isNotNull(labResults.get("hbsAgOutcome")), Form129DiseaseType.HEPATITIS_B.ordinal())
                .when(cb.isNotNull(labResults.get("rwOutcome")), Form129DiseaseType.SYPHILIS.ordinal())
                .otherwise(Form129DiseaseType.UNKNOWN.ordinal());
    }
}
