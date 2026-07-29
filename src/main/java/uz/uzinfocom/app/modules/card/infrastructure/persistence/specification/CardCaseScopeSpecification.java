package uz.uzinfocom.app.modules.card.infrastructure.persistence.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;

/**
 * Organization-scope predicate for a {@link Card} that now belongs to
 * either {@code form058} or {@code form0581} — shared by {@code
 * CardQueryService} (the "mine, broader scope" listing) and {@code
 * CardStatsRepository}/{@code ActStatsRepository} (dashboard aggregates),
 * so the "whichever parent is set" branching lives in exactly one place.
 * {@code cardPath} accepts any {@link Path} to a {@code Card} — the query
 * root itself for card queries, or {@code root.get("card")} one hop further
 * for act queries — entirely DB-level (Criteria API → SQL {@code WHERE ...
 * AND (... OR ...)}), no Java-side filtering.
 */
public final class CardCaseScopeSpecification {

    private CardCaseScopeSpecification() {
    }

    public static Predicate scopePredicate(
            Path<Card> cardPath,
            CriteriaBuilder cb,
            SenderReceiverScopePredicateFactory scopePredicateFactory,
            ResolvedOrganizationScope scope,
            CaseFormType formType
    ) {
        Predicate form058Branch = cb.and(
                cb.isNotNull(cardPath.get("form058")),
                scopePredicateFactory.applyDirectionScope(cardPath.get("form058"), cb, scope, true)
        );
        Predicate form0581Branch = cb.and(
                cb.isNotNull(cardPath.get("form0581")),
                scopePredicateFactory.applyDirectionScope(cardPath.get("form0581"), cb, scope, true)
        );

        return switch (formType) {
            case ANY -> cb.or(form058Branch, form0581Branch);
            case FORM058 -> form058Branch;
            case FORM0581 -> form0581Branch;
        };
    }
}
