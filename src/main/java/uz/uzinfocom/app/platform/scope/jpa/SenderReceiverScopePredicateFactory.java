package uz.uzinfocom.app.platform.scope.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;

/**
 * Direction-based organization scope for any entity that carries a
 * {@code senderOrganizationId}/{@code receiverOrganizationId} pair
 * (Form058, Form0581, and anything joined to one of them, e.g. Card/Act via
 * their owning Form058). Every such entity needs the exact same three rules:
 * <p>
 * received = true  -> incoming records, scope by receiverOrganizationId
 * received = false -> outgoing records, scope by senderOrganizationId
 * received = null  -> all visible records, sender OR receiver scope
 * <p>
 * This used to be duplicated verbatim as {@code Form058ScopePredicateFactory}
 * and {@code Form0581ScopePredicateFactory} — one generic component replaces
 * both, since the field names and logic never actually differed between them.
 */
@Component
@RequiredArgsConstructor
public class SenderReceiverScopePredicateFactory {

    private static final String SENDER_ORGANIZATION_ID = "senderOrganizationId";
    private static final String RECEIVER_ORGANIZATION_ID = "receiverOrganizationId";

    private final OrganizationScopePredicateFactory scopePredicateFactory;

    public <T> Predicate applyDirectionScope(
            Path<T> root,
            CriteriaBuilder cb,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        if (received == null) {
            return cb.or(
                    scopePredicateFactory.apply(root, cb, SENDER_ORGANIZATION_ID, scope),
                    scopePredicateFactory.apply(root, cb, RECEIVER_ORGANIZATION_ID, scope)
            );
        }

        String organizationField = Boolean.TRUE.equals(received)
                ? RECEIVER_ORGANIZATION_ID
                : SENDER_ORGANIZATION_ID;

        return scopePredicateFactory.apply(root, cb, organizationField, scope);
    }
}
