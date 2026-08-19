package uz.uzinfocom.app.modules.card.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.card.application.query.dto.CardDailyCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.specification.CardCaseScopeSpecification;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.stats.AbstractCaseStatsRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Aggregation queries for the home dashboard's card widget. {@link Card} has
 * no organization-id field of its own — only an inherited UUID audit column —
 * so scope is resolved by navigating to the owning {@link Form058}/{@link
 * Form0581} case (a card belongs to exactly one of the two) and reusing
 * {@link SenderReceiverScopePredicateFactory} via {@link CardCaseScopeSpecification},
 * exactly as the regular Form058/Form0581 stats queries do. Cards only ever
 * exist on the receiving (investigating) side of a case, so {@code received}
 * is always {@code true}. Card is soft-deletable via {@code deleteInfo.deleted}
 * like Form058/Form0581, so the base class's default {@code notDeleted}
 * applies unmodified.
 * <p>
 * {@code countByStatus}/{@code countTotal}/{@code countActive} are all
 * year-to-date (since {@link #startOfCurrentYear()}) — {@code countByMonth}
 * is unaffected since it already takes its own {@code [from, to]} window.
 */
@Repository
public class CardStatsRepository extends AbstractCaseStatsRepository<Card> {

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;

    public CardStatsRepository(EntityManager entityManager, SenderReceiverScopePredicateFactory scopePredicateFactory) {
        super(entityManager, Card.class);
        this.scopePredicateFactory = scopePredicateFactory;
    }

    public List<CardStatusCountResponse> countByStatus(ResolvedOrganizationScope scope, CaseFormType formType) {
        return countGrouped(
                (root, cb) -> root.<CardStatus>get("status"),
                (root, cb) -> cb.and(ownerScope(root, cb, scope, formType), createdAtSince(root, cb, startOfCurrentYear())),
                CardStatusCountResponse::new
        );
    }

    public List<CardTypeCountResponse> countByType(ResolvedOrganizationScope scope) {
        return countGrouped(
                (root, cb) -> root.<CardType>get("cardType"),
                (root, cb) -> cb.and(
                        ownerScope(root, cb, scope, CaseFormType.ANY), createdAtSince(root, cb, startOfCurrentYear())
                ),
                CardTypeCountResponse::new
        );
    }

    /** Total card count in scope, year-to-date — a direct {@code COUNT(*)}, not a sum over {@link #countByStatus}. */
    public long countTotal(ResolvedOrganizationScope scope, CaseFormType formType) {
        return countAll((root, cb) ->
                cb.and(ownerScope(root, cb, scope, formType), createdAtSince(root, cb, startOfCurrentYear())));
    }

    /**
     * Count of cards not yet approved by a supervisor (every status except
     * {@link CardStatus#APPROVED}), year-to-date — a direct {@code COUNT(*)
     * WHERE status <> 'APPROVED'}, not a subtraction over {@link #countByStatus}.
     */
    public long countActive(ResolvedOrganizationScope scope, CaseFormType formType) {
        return countAll((root, cb) -> cb.and(
                ownerScope(root, cb, scope, formType),
                createdAtSince(root, cb, startOfCurrentYear()),
                cb.notEqual(root.get("status"), CardStatus.APPROVED)
        ));
    }

    /**
     * Monthly trend — for the home dashboard's card dynamics chart, same
     * shape/window as {@code Form058StatsRepository.countByMonth}. Not
     * year-to-date restricted beyond its own {@code [from, to]} window, and
     * always {@link CaseFormType#ANY} (both form058- and form0581-owned
     * cards) — this is the standalone card dashboard's chart, not embedded
     * per-form.
     */
    public List<CardDailyCountResponse> countByMonth(ResolvedOrganizationScope scope, LocalDate from, LocalDate to) {
        return countByDateBucket(
                "month",
                (root, cb) -> ownerScope(root, cb, scope, CaseFormType.ANY),
                from, to,
                CardDailyCountResponse::new
        );
    }

    private Predicate ownerScope(Root<Card> root, CriteriaBuilder cb, ResolvedOrganizationScope scope, CaseFormType formType) {
        return CardCaseScopeSpecification.scopePredicate(root, cb, scopePredicateFactory, scope, formType);
    }
}
