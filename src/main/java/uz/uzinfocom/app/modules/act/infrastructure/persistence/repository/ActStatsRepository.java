package uz.uzinfocom.app.modules.act.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDailyCountResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.specification.CardCaseScopeSpecification;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.stats.jpa.AbstractCaseStatsRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Aggregation queries for the home dashboard's act widget. {@link Act}, like
 * {@link Card}, has no organization-id field of its own — scope is resolved
 * by navigating act -> card -> (form058 or form0581) and reusing {@link
 * SenderReceiverScopePredicateFactory} via {@link CardCaseScopeSpecification}
 * (one hop further than {@code CardStatsRepository} — {@code root.get("card")}
 * instead of the query root itself). {@code received} is always {@code true}
 * for the same reason as {@code CardStatsRepository}. Act is soft-deletable
 * via {@code deleteInfo.deleted} like Form058/Form0581, so the base class's
 * default {@code notDeleted} applies unmodified.
 * <p>
 * {@code countByStatus}/{@code countTotal} are year-to-date (since {@link
 * #startOfCurrentYear()}) — {@code countByMonth} is unaffected since it
 * already takes its own {@code [from, to]} window.
 */
@Repository
public class ActStatsRepository extends AbstractCaseStatsRepository<Act> {

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;

    public ActStatsRepository(EntityManager entityManager, SenderReceiverScopePredicateFactory scopePredicateFactory) {
        super(entityManager, Act.class);
        this.scopePredicateFactory = scopePredicateFactory;
    }

    public List<ActStatusCountResponse> countByStatus(ResolvedOrganizationScope scope, CaseFormType formType) {
        return countGrouped(
                (root, cb) -> root.<ActStatus>get("actStatus"),
                (root, cb) -> cb.and(ownerScope(root, cb, scope, formType), createdAtSince(root, cb, startOfCurrentYear())),
                ActStatusCountResponse::new
        );
    }

    /** Total act count in scope, year-to-date — a direct {@code COUNT(*)}, not a sum over {@link #countByStatus}. */
    public long countTotal(ResolvedOrganizationScope scope, CaseFormType formType) {
        return countAll((root, cb) ->
                cb.and(ownerScope(root, cb, scope, formType), createdAtSince(root, cb, startOfCurrentYear())));
    }

    /**
     * Monthly trend — for the home dashboard's act dynamics chart, same
     * shape/window as {@code Form058StatsRepository.countByMonth}. Not
     * year-to-date restricted beyond its own {@code [from, to]} window, and
     * always {@link CaseFormType#ANY}, same reasoning as {@code
     * CardStatsRepository.countByMonth}.
     */
    public List<ActDailyCountResponse> countByMonth(ResolvedOrganizationScope scope, LocalDate from, LocalDate to) {
        return countByDateBucket(
                "month",
                (root, cb) -> ownerScope(root, cb, scope, CaseFormType.ANY),
                from, to,
                ActDailyCountResponse::new
        );
    }

    private Predicate ownerScope(Root<Act> root, CriteriaBuilder cb, ResolvedOrganizationScope scope, CaseFormType formType) {
        return CardCaseScopeSpecification.scopePredicate(root.get("card"), cb, scopePredicateFactory, scope, formType);
    }
}
