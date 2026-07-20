package uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581DailyCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581Mkb10CountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581OrganizationCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581SourceCountResponse;
import uz.uzinfocom.app.modules.form0581.application.stats.query.dto.Form0581StatusCountResponse;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.stats.jpa.AbstractCaseStatsRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Aggregation queries for the stats dashboard. Built on
 * {@link AbstractCaseStatsRepository} (the shared Criteria API plumbing) and
 * {@link SenderReceiverScopePredicateFactory} (the shared sender/receiver
 * scope rule) so that only the entity type, the per-query dimension, and the
 * response mapping are written here — the query-building itself is written
 * exactly once, in the shared base class.
 */
@Repository
public class Form0581StatsRepository extends AbstractCaseStatsRepository<Form0581> {

    private static final List<Form0581Status> PENDING_STATUSES = Arrays.stream(Form0581Status.values())
            .filter(Form0581Status::isApprovalDecisionPending)
            .toList();

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;

    public Form0581StatsRepository(EntityManager entityManager, SenderReceiverScopePredicateFactory scopePredicateFactory) {
        super(entityManager, Form0581.class);
        this.scopePredicateFactory = scopePredicateFactory;
    }

    /** Total case count in scope — a direct {@code COUNT(*)}, not a sum over {@link #countByStatus}. */
    public long countTotal(ResolvedOrganizationScope scope, Boolean received) {
        return countAll((root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received));
    }

    /**
     * Count of cases without a final approval decision yet ({@link
     * Form0581Status#isApprovalDecisionPending()}) — a direct {@code COUNT(*)},
     * not a Java-side filter+sum over {@link #countByStatus}.
     */
    public long countActive(ResolvedOrganizationScope scope, Boolean received) {
        return countAll((root, cb) -> cb.and(
                scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                root.<Form0581Status>get("status").in(PENDING_STATUSES)
        ));
    }

    public List<Form0581StatusCountResponse> countByStatus(ResolvedOrganizationScope scope, Boolean received) {
        return countGrouped(
                (root, cb) -> root.<Form0581Status>get("status"),
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                Form0581StatusCountResponse::new
        );
    }

    /**
     * Grouped by source (e.g. MANUAL/QR/DMED) — for the home dashboard's
     * source breakdown.
     */
    public List<Form0581SourceCountResponse> countBySource(ResolvedOrganizationScope scope, Boolean received) {
        return countGrouped(
                (root, cb) -> root.<String>get("source"),
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                Form0581SourceCountResponse::new
        );
    }

    public List<Form0581Mkb10CountResponse> topMkb10(ResolvedOrganizationScope scope, Boolean received, int limit) {
        return topGrouped(
                this::mkb10Code,
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                limit,
                Form0581Mkb10CountResponse::new
        );
    }

    public List<Form0581DailyCountResponse> countByDay(
            ResolvedOrganizationScope scope,
            Boolean received,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return countByDateBucket(
                "day",
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                fromDate, toDate,
                Form0581DailyCountResponse::new
        );
    }

    /**
     * Same as {@link #countByDay}, bucketed by calendar month instead of day —
     * for the home dashboard's multi-month trend chart.
     */
    public List<Form0581DailyCountResponse> countByMonth(
            ResolvedOrganizationScope scope,
            Boolean received,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return countByDateBucket(
                "month",
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                fromDate, toDate,
                Form0581DailyCountResponse::new
        );
    }

    /**
     * Filtered version of {@link #countByReceiverOrganization} — restricted to
     * a specific set of organization ids (e.g. every organization within one
     * region), for the home dashboard's district/region breakdown. Unlike the
     * unscoped admin variant, this is safe for non-admin callers precisely
     * because the id set is already scope-resolved by the caller.
     */
    public List<Form0581OrganizationCountResponse> countByReceiverOrganizationWithinIds(List<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        return countGrouped(
                (root, cb) -> root.<Long>get("receiverOrganizationId"),
                (root, cb) -> root.<Long>get("receiverOrganizationId").in(organizationIds),
                Form0581OrganizationCountResponse::new
        );
    }

    /**
     * Unscoped (all organizations) version of {@link #countByStatus} — for
     * the admin dashboard only. Callers must gate this behind an admin-only
     * authorization check themselves.
     */
    public List<Form0581StatusCountResponse> countByStatusUnscoped() {
        return countGrouped(
                (root, cb) -> root.<Form0581Status>get("status"),
                null,
                Form0581StatusCountResponse::new
        );
    }

    /**
     * Unscoped (all organizations) version of {@link #topMkb10} — for the
     * admin dashboard only. Callers must gate this behind an admin-only
     * authorization check themselves.
     */
    public List<Form0581Mkb10CountResponse> topMkb10Unscoped(int limit) {
        return topGrouped(
                this::mkb10Code,
                null,
                limit,
                Form0581Mkb10CountResponse::new
        );
    }

    /**
     * Unscoped (all organizations) version of {@link #countByDay} — for the
     * admin dashboard only. Callers must gate this behind an admin-only
     * authorization check themselves.
     */
    public List<Form0581DailyCountResponse> countByDayUnscoped(LocalDate fromDate, LocalDate toDate) {
        return countByDateBucket("day", null, fromDate, toDate, Form0581DailyCountResponse::new);
    }

    /**
     * Cross-organization breakdown — no scope predicate at all. Callers must
     * gate this behind an admin-only authorization check themselves.
     */
    public List<Form0581OrganizationCountResponse> countBySenderOrganization() {
        return countGrouped(
                (root, cb) -> root.<Long>get("senderOrganizationId"),
                null,
                Form0581OrganizationCountResponse::new
        );
    }

    /**
     * Cross-organization breakdown — no scope predicate at all. Callers must
     * gate this behind an admin-only authorization check themselves.
     */
    public List<Form0581OrganizationCountResponse> countByReceiverOrganization() {
        return countGrouped(
                (root, cb) -> root.<Long>get("receiverOrganizationId"),
                null,
                Form0581OrganizationCountResponse::new
        );
    }

    private Path<String> mkb10Code(Root<Form0581> root, CriteriaBuilder cb) {
        return root.get("diagnosisInfo").get("mkb10Code");
    }
}
