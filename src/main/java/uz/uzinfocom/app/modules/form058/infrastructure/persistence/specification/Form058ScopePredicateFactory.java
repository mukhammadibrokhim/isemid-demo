package uz.uzinfocom.app.modules.form058.infrastructure.persistence.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopePredicateFactory;

@Component
@RequiredArgsConstructor
public class Form058ScopePredicateFactory {

    private static final String SENDER_ORGANIZATION_ID = "senderOrganizationId";
    private static final String RECEIVER_ORGANIZATION_ID = "receiverOrganizationId";

    private final OrganizationScopePredicateFactory scopePredicateFactory;

    /**
     * Direction-based Form058 scope:
     * <p>
     * received = true  -> incoming records, scope by receiverOrganizationId
     * received = false -> outgoing records, scope by senderOrganizationId
     * received = null  -> all visible records, sender OR receiver scope
     */
    public Predicate applyDirectionScope(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        if (received == null) {
            return cb.or(
                    scopePredicateFactory.apply(
                            root,
                            cb,
                            SENDER_ORGANIZATION_ID,
                            scope
                    ),
                    scopePredicateFactory.apply(
                            root,
                            cb,
                            RECEIVER_ORGANIZATION_ID,
                            scope
                    )
            );
        }

        String organizationField = Boolean.TRUE.equals(received)
                ? RECEIVER_ORGANIZATION_ID
                : SENDER_ORGANIZATION_ID;

        return scopePredicateFactory.apply(
                root,
                cb,
                organizationField,
                scope
        );
    }
}