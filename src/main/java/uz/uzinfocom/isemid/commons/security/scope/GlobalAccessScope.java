package uz.uzinfocom.isemid.commons.security.scope;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;

import java.util.UUID;

/**
 * Builds reusable JPA {@link Specification} fragments for organization-based access filtering.
 *
 * <p>This component is intentionally generic and does not know anything about Form058-specific
 * business semantics such as sender/receiver logic. It only knows how to convert a resolved
 * organization scope into a database predicate for a given organization path.
 *
 * <p>Example usage:
 * <pre>
 *     globalAccessScope.specification("senderOrganization", scope)
 *     globalAccessScope.specification("receiverOrganization", scope)
 *     globalAccessScope.specification("screening.createdOrg", scope)
 * </pre>
 *
 * <p>This keeps all low-level join and predicate building in one place and avoids duplicating
 * organization filtering logic across features.
 */
@Component
public class GlobalAccessScope {

    /**
     * Builds a JPA specification for the given organization association path.
     *
     * @param organizationPath association path from the root entity to an {@code Organization}
     *                         entity, for example {@code createdOrg} or {@code senderOrganization}
     * @param scope already resolved access scope for the current user organization
     * @param <T> root entity type
     * @return JPA specification that applies the organization access rule
     */
    public <T> Specification<T> specification(String organizationPath, ResolvedAccessScope scope) {
        return (root, query, cb) -> {
            // Empty scope must never accidentally open access.
            if (scope == null || scope.isEmpty()) {
                return cb.disjunction();
            }

            // ALL means no organization restriction is needed.
            if (scope.mode() == ResolvedAccessScope.Mode.ALL) {
                return cb.conjunction();
            }

            // NONE means the query must return no rows.
            if (scope.mode() == ResolvedAccessScope.Mode.NONE) {
                return cb.disjunction();
            }

            Path<?> orgPath = resolveAssociationPath(root, organizationPath);
            return buildOrganizationPredicate(orgPath, cb, scope);
        };
    }

    /**
     * Converts a resolved scope into a concrete predicate over an {@code Organization} path.
     *
     * <p>Expected organization fields:
     * <ul>
     *     <li>{@code uuid}</li>
     *     <li>{@code stateCode}</li>
     *     <li>{@code levelType}</li>
     * </ul>
     */
    public Predicate buildOrganizationPredicate(Path<?> orgPath, CriteriaBuilder cb, ResolvedAccessScope scope) {
        if (scope == null || scope.isEmpty()) {
            return cb.disjunction();
        }

        Path<String> stateCodePath = orgPath.get("stateCode");
        Path<LevelType> levelTypePath = orgPath.get("levelType");
        Path<UUID> uuidPath = orgPath.get("uuid");

        return switch (scope.mode()) {
            case ALL -> cb.conjunction();

            case STATE_NON_REPUBLICAN -> cb.and(
                    // Same state only
                    cb.equal(stateCodePath, scope.stateCode()),
                    // Exclude republican-level nodes to avoid leaking top-level visibility
                    cb.notEqual(levelTypePath, LevelType.REPUBLICAN)
            );

            case DIRECT_ORGANIZATION ->
                // Strict access to exactly one organization
                    cb.equal(uuidPath, scope.organizationUuid());

            case NONE -> cb.disjunction();
        };
    }

    /**
     * Resolves a nested association path into a joined path.
     *
     * <p>For example:
     * <ul>
     *     <li>{@code createdOrg}</li>
     *     <li>{@code screening.createdOrg}</li>
     *     <li>{@code senderOrganization}</li>
     * </ul>
     *
     * <p>{@link JoinType#INNER} is used intentionally because access filtering requires that the
     * organization relation is present; this also helps avoid weaker predicates and unnecessary
     * null-side rows.
     */
    private Path<?> resolveAssociationPath(From<?, ?> root, String associationPath) {
        String[] parts = associationPath.split("\\.");
        From<?, ?> current = root;

        for (String part : parts) {
            current = current.join(part, JoinType.INNER);
        }

        return current;
    }
}