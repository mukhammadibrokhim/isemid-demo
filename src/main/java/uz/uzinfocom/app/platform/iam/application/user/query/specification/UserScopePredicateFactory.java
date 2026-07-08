package uz.uzinfocom.app.platform.iam.application.user.query.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopePredicateFactory;

@Component
@RequiredArgsConstructor
public class UserScopePredicateFactory {

    private static final String ORGANIZATIONS = "organizations";

    private final OrganizationScopePredicateFactory scopePredicateFactory;

    /**
     * Restricts a User query to users who belong to at least one organization
     * within the caller's current organization scope.
     */
    public Predicate applyOrganizationScope(
            Root<User> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            ResolvedOrganizationScope scope
    ) {
        return scopePredicateFactory.applyToCollection(root, query, cb, ORGANIZATIONS, scope);
    }
}
