package uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058DailyCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058Mkb10CountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058OrganizationCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058SourceCountResponse;
import uz.uzinfocom.app.modules.form058.application.stats.query.dto.Form058StatusCountResponse;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.stats.jpa.AbstractCaseStatsRepository;

import java.time.LocalDate;
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
public class Form058StatsRepository extends AbstractCaseStatsRepository<Form058> {

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;

    public Form058StatsRepository(EntityManager entityManager, SenderReceiverScopePredicateFactory scopePredicateFactory) {
        super(entityManager, Form058.class);
        this.scopePredicateFactory = scopePredicateFactory;
    }

    public List<Form058StatusCountResponse> countByStatus(ResolvedOrganizationScope scope, Boolean received) {
        return countGrouped(
                (root, cb) -> root.<FormStatus>get("status"),
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                Form058StatusCountResponse::new
        );
    }

    /**
     * Grouped by source (e.g. MANUAL/QR/DMED) — for the home dashboard's
     * source breakdown.
     */
    public List<Form058SourceCountResponse> countBySource(ResolvedOrganizationScope scope, Boolean received) {
        return countGrouped(
                (root, cb) -> root.<String>get("source"),
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                Form058SourceCountResponse::new
        );
    }

    public List<Form058Mkb10CountResponse> topMkb10(ResolvedOrganizationScope scope, Boolean received, int limit) {
        return topGrouped(
                this::mkb10Code,
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                limit,
                Form058Mkb10CountResponse::new
        );
    }

    public List<Form058DailyCountResponse> countByDay(
            ResolvedOrganizationScope scope,
            Boolean received,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return countByDateBucket(
                "day",
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                fromDate, toDate,
                Form058DailyCountResponse::new
        );
    }

    /**
     * Same as {@link #countByDay}, bucketed by calendar month instead of day —
     * for the home dashboard's multi-month trend chart.
     */
    public List<Form058DailyCountResponse> countByMonth(
            ResolvedOrganizationScope scope,
            Boolean received,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return countByDateBucket(
                "month",
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root, cb, scope, received),
                fromDate, toDate,
                Form058DailyCountResponse::new
        );
    }

    /**
     * Filtered version of {@link #countByReceiverOrganization} — restricted to
     * a specific set of organization ids (e.g. every organization within one
     * region), for the home dashboard's district/region breakdown. Unlike the
     * unscoped admin variant, this is safe for non-admin callers precisely
     * because the id set is already scope-resolved by the caller.
     */
    public List<Form058OrganizationCountResponse> countByReceiverOrganizationWithinIds(List<Long> organizationIds) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return List.of();
        }

        return countGrouped(
                (root, cb) -> root.<Long>get("receiverOrganizationId"),
                (root, cb) -> root.<Long>get("receiverOrganizationId").in(organizationIds),
                Form058OrganizationCountResponse::new
        );
    }

    /**
     * Unscoped (all organizations) version of {@link #countByStatus} — for
     * the admin dashboard only. Callers must gate this behind an admin-only
     * authorization check themselves.
     */
    public List<Form058StatusCountResponse> countByStatusUnscoped() {
        return countGrouped(
                (root, cb) -> root.<FormStatus>get("status"),
                null,
                Form058StatusCountResponse::new
        );
    }

    /**
     * Unscoped (all organizations) version of {@link #topMkb10} — for the
     * admin dashboard only. Callers must gate this behind an admin-only
     * authorization check themselves.
     */
    public List<Form058Mkb10CountResponse> topMkb10Unscoped(int limit) {
        return topGrouped(
                this::mkb10Code,
                null,
                limit,
                Form058Mkb10CountResponse::new
        );
    }

    /**
     * Unscoped (all organizations) version of {@link #countByDay} — for the
     * admin dashboard only. Callers must gate this behind an admin-only
     * authorization check themselves.
     */
    public List<Form058DailyCountResponse> countByDayUnscoped(LocalDate fromDate, LocalDate toDate) {
        return countByDateBucket("day", null, fromDate, toDate, Form058DailyCountResponse::new);
    }

    /**
     * Cross-organization breakdown — no scope predicate at all. Callers must
     * gate this behind an admin-only authorization check themselves.
     */
    public List<Form058OrganizationCountResponse> countBySenderOrganization() {
        return countGrouped(
                (root, cb) -> root.<Long>get("senderOrganizationId"),
                null,
                Form058OrganizationCountResponse::new
        );
    }

    /**
     * Cross-organization breakdown — no scope predicate at all. Callers must
     * gate this behind an admin-only authorization check themselves.
     */
    public List<Form058OrganizationCountResponse> countByReceiverOrganization() {
        return countGrouped(
                (root, cb) -> root.<Long>get("receiverOrganizationId"),
                null,
                Form058OrganizationCountResponse::new
        );
    }

    private Path<String> mkb10Code(Root<Form058> root, CriteriaBuilder cb) {
        return root.get("diagnosisInfo").get("mkb10Code");
    }
}
