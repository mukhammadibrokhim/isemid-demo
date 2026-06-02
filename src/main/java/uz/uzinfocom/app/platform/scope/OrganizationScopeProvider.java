package uz.uzinfocom.app.platform.scope;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class OrganizationScopeProvider {

    private final OrganizationScopeResolver scopeResolver;
    private final CriteriaPathResolver pathResolver;

    public <T> Specification<T> byCurrentOrganization(String organizationPath) {
        Organization organization = CurrentOrganizationContext.require();
        ResolvedOrganizationScope scope = scopeResolver.resolve(organization);
        return byScope(scope, organizationPath);
    }

    public <T> Specification<T> byScope(ResolvedOrganizationScope scope, String organizationPath) {
        return (root, query, cb) -> {
            Path<?> path = pathResolver.resolve(root, organizationPath);
            return getPredicate(scope, cb, path);
        };
    }

    public <T> Specification<T> byCurrentOrganizationAny(String... organizationPaths) {
        Organization organization = CurrentOrganizationContext.require();
        ResolvedOrganizationScope scope = scopeResolver.resolve(organization);

        return (root, query, cb) -> {
            if (organizationPaths == null || organizationPaths.length == 0) {
                throw new IllegalArgumentException("At least one organization path is required");
            }

            Predicate[] predicates = Arrays.stream(organizationPaths)
                    .map(path -> buildScopePredicate(
                            pathResolver.resolve(root, path),
                            scope,
                            cb
                    ))
                    .toArray(Predicate[]::new);

            return cb.or(predicates);
        };
    }

    private Predicate buildScopePredicate(
            Path<?> organizationPath,
            ResolvedOrganizationScope scope,
            CriteriaBuilder cb
    ) {
        return getPredicate(scope, cb, organizationPath);
    }


    private Predicate getPredicate(ResolvedOrganizationScope scope, CriteriaBuilder cb, Path<?> path) {
        return switch (scope.mode()) {
            case ALL -> cb.conjunction();
            case STATE -> cb.equal(path.get("stateCode"), scope.stateCode());
            case CITY -> cb.equal(path.get("cityCode"), scope.cityCode());
            case ORGANIZATION -> cb.equal(path.get("uuid"), scope.organizationUuid());
        };
    }

}
