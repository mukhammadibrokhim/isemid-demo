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
                            query,
                            cb,
                            SENDER_ORGANIZATION_ID,
                            scope
                    ),
                    scopePredicateFactory.apply(
                            root,
                            query,
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
                query,
                cb,
                organizationField,
                scope
        );
    }
}