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
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.stats.jpa.AbstractCaseStatsRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Aggregation queries for the home dashboard's card widget. {@link Card} has
 * no organization-id field of its own — only an inherited UUID audit column —
 * so scope is resolved by navigating to the owning {@link Form058} case and
 * reusing {@link SenderReceiverScopePredicateFactory} verbatim, exactly as the
 * regular Form058 stats queries do. Cards only ever exist on the receiving
 * (investigating) side of a case, so {@code received} is always {@code true}.
 * Card has no soft-delete flag, unlike Form058/Form0581, hence the
 * {@link #notDeleted} override.
 */
@Repository
public class CardStatsRepository extends AbstractCaseStatsRepository<Card> {

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;

    public CardStatsRepository(EntityManager entityManager, SenderReceiverScopePredicateFactory scopePredicateFactory) {
        super(entityManager, Card.class);
        this.scopePredicateFactory = scopePredicateFactory;
    }

    @Override
    protected Predicate notDeleted(Root<Card> root, CriteriaBuilder cb) {
        return cb.conjunction();
    }

    public List<CardStatusCountResponse> countByStatus(ResolvedOrganizationScope scope) {
        return countGrouped(
                (root, cb) -> root.<CardStatus>get("status"),
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root.get("form058"), cb, scope, true),
                (status, count) -> new CardStatusCountResponse(status, count)
        );
    }

    public List<CardTypeCountResponse> countByType(ResolvedOrganizationScope scope) {
        return countGrouped(
                (root, cb) -> root.<CardType>get("cardType"),
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root.get("form058"), cb, scope, true),
                (type, count) -> new CardTypeCountResponse(type, count)
        );
    }

    /** Total card count in scope — a direct {@code COUNT(*)}, not a sum over {@link #countByStatus}. */
    public long countTotal(ResolvedOrganizationScope scope) {
        return countAll((root, cb) -> scopePredicateFactory.applyDirectionScope(root.get("form058"), cb, scope, true));
    }

    /**
     * Count of cards not yet approved by a supervisor (every status except
     * {@link CardStatus#APPROVED}) — a direct {@code COUNT(*) WHERE status <> 'APPROVED'},
     * not a subtraction over {@link #countByStatus}.
     */
    public long countActive(ResolvedOrganizationScope scope) {
        return countAll((root, cb) -> cb.and(
                scopePredicateFactory.applyDirectionScope(root.get("form058"), cb, scope, true),
                cb.notEqual(root.get("status"), CardStatus.APPROVED)
        ));
    }

    /**
     * Monthly trend — for the home dashboard's card dynamics chart, same
     * shape/window as {@code Form058StatsRepository.countByMonth}.
     */
    public List<CardDailyCountResponse> countByMonth(ResolvedOrganizationScope scope, LocalDate from, LocalDate to) {
        return countByDateBucket(
                "month",
                (root, cb) -> scopePredicateFactory.applyDirectionScope(root.get("form058"), cb, scope, true),
                from, to,
                CardDailyCountResponse::new
        );
    }
}
