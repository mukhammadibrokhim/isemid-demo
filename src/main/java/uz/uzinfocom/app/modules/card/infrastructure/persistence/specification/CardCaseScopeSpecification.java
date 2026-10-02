package uz.uzinfocom.app.modules.card.infrastructure.persistence.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.SenderReceiverScopePredicateFactory;

/**
 * Organization-scope predicate for a {@link Card} that now belongs to
 * either {@code form058} or {@code form0581} — shared by {@code
 * CardQueryService} (the "mine, broader scope" listing) and {@code
 * CardStatsRepository}/{@code ActStatsRepository} (dashboard aggregates),
 * so the "whichever parent is set" branching lives in exactly one place.
 * {@code cardPath} accepts any {@link From} rooted at {@code Card} — the
 * query root itself for card queries, or an explicit join one hop further
 * for act queries — entirely DB-level (Criteria API → SQL {@code WHERE ...
 * AND (... OR ...)}), no Java-side filtering.
 * <p>
 * {@code form058}/{@code form0581} are joined explicitly as {@code LEFT}
 * (not navigated via plain {@code Path.get()}) on purpose: a card has
 * exactly one of the two set, never both ({@code
 * chk_card_exactly_one_form}), so navigating both via {@code Path.get()}
 * registers two implicit INNER joins in the same FROM clause — no row can
 * satisfy both at once, so the query returns zero rows regardless of the
 * OR below.
 */
public final class CardCaseScopeSpecification {

    private CardCaseScopeSpecification() {
    }

    public static Predicate scopePredicate(
            From<?, Card> cardPath,
            CriteriaBuilder cb,
            SenderReceiverScopePredicateFactory scopePredicateFactory,
            ResolvedOrganizationScope scope,
            CaseFormType formType
    ) {
        Join<Card, Form058> form058Join = cardPath.join("form058", JoinType.LEFT);
        Join<Card, Form0581> form0581Join = cardPath.join("form0581", JoinType.LEFT);

        Predicate form058Branch = cb.and(
                cb.isNotNull(form058Join.get("id")),
                scopePredicateFactory.applyDirectionScope(form058Join, cb, scope, true)
        );
        Predicate form0581Branch = cb.and(
                cb.isNotNull(form0581Join.get("id")),
                scopePredicateFactory.applyDirectionScope(form0581Join, cb, scope, true)
        );

        return switch (formType) {
            case ANY -> cb.or(form058Branch, form0581Branch);
            case FORM058 -> form058Branch;
            case FORM0581 -> form0581Branch;
        };
    }

    /**
     * Single-card lookup counterpart to {@link #scopePredicate} — same
     * receiver-side org scope, plus an id match and the soft-delete guard,
     * so {@code CardQueryService#getById}/{@code #getPdf} 404 (not 403) on a
     * card outside the caller's scope instead of returning it to anyone who
     * knows the id.
     */
    public static Specification<Card> visibleById(
            Long id,
            SenderReceiverScopePredicateFactory scopePredicateFactory,
            ResolvedOrganizationScope scope
    ) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("id"), id),
                cb.isFalse(root.get("deleteInfo").get("deleted")),
                scopePredicate(root, cb, scopePredicateFactory, scope, CaseFormType.ANY)
        );
    }
}
